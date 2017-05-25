package uk.ac.ebi.subs.validator.tester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.tester.submissions.SubmissionPublisher;
import uk.ac.ebi.subs.validator.tester.utils.ValidationOutcomeProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Create and publish a defined number of submission with a random number of samples
 * and meanwhile update some of them and publish them, as well
 *
 * Created by karoly on 16/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@Category(RabbitMQDependentTest.class)
public class PublishManySubmissionsTest {

    @Autowired
    SubmissionPublisher publisher;

    private Map<String, SubmissionEnvelope> publishedSubmissions = new HashMap<>();

    private Map<String, ValidationOutcomeProperties> submissionsToCheck = new LinkedHashMap<>();

    @Test
    public void createAndPublishManySubmissionAndRandomlyUpdatesThem() throws IOException {
        int numberOfSubmissionsToCreate = 20;
        int updateNthSubmission = 3;
        List<SubmissionEnvelope> submissionEnvelopes = publisher.createManySubmissionsToPublish(numberOfSubmissionsToCreate);
        int publishedCount = 0;
        for (SubmissionEnvelope submissionEnvelope: submissionEnvelopes) {
            publisher.publishASubmisssionEnvelope(submissionEnvelope);

            publishedSubmissions.put(submissionEnvelope.getSubmission().getId(), submissionEnvelope);

            populateSubmissionsToCheck(submissionEnvelope);

            // update every 'Nth' submission after published 5
            if (++publishedCount > 5 && (publishedCount % updateNthSubmission == 0)) {
                SubmissionEnvelope updatedSubmissionEnvelopToPublish = publisher.updateSubmission(getRandomPublishedSubmission());
                publisher.publishASubmisssionEnvelope(updatedSubmissionEnvelopToPublish);

                updateSubmissionsToCheck(updatedSubmissionEnvelopToPublish);
            }
        }

        generateSubmissionsResultFile();
    }

    private SubmissionEnvelope getRandomPublishedSubmission() {
        List<String> publishedSubmissionIds = new ArrayList<>(publishedSubmissions.keySet());

        String randomKey = publishedSubmissionIds.get(ThreadLocalRandom.current().nextInt(0, publishedSubmissionIds.size()));

        return publishedSubmissions.get(randomKey);
    }

    private void populateSubmissionsToCheck(SubmissionEnvelope submissionEnvelope) {
        String submissionUuid = getSubmissionId(submissionEnvelope);
        ValidationOutcomeProperties outcomeProperties =
                new ValidationOutcomeProperties(submissionEnvelope.getSamples().get(0).getId());

        submissionsToCheck.put(submissionUuid, outcomeProperties);
    }

    private String getSubmissionId(SubmissionEnvelope submissionEnvelope) {
        return submissionEnvelope.getSubmission().getId();
    }

    private void updateSubmissionsToCheck(SubmissionEnvelope submissionEnvelope) {
        String submissionUuid = getSubmissionId(submissionEnvelope);

        ValidationOutcomeProperties propertiesToUpdate = submissionsToCheck.get(submissionUuid);
        propertiesToUpdate.incrementVersion();

        submissionsToCheck.put(submissionUuid, propertiesToUpdate);
    }

    private void generateSubmissionsResultFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String basePath = "src/test/resources/generated";
        String filePath = basePath + "/generatedSubmissions_" + getFormattedCurrentDateTimeAsString() + ".json";
        String configFilePath = basePath + "/lastGeneratedFile.txt";

        File resultFile = new File(filePath);
        resultFile.getParentFile().mkdirs();

        mapper.writeValue(resultFile, submissionsToCheck);

        Files.write(Paths.get(configFilePath), Arrays.asList(filePath));
    }

    private String getFormattedCurrentDateTimeAsString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        return now.format(formatter);
    }
}
