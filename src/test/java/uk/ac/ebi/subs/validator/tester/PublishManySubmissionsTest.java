package uk.ac.ebi.subs.validator.tester;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.tester.submissions.SubmissionPublisher;

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

    @Test
    public void createAndPublishManySubmissionAndRandomlyUpdatesThem() {
        int numberOfSubmissionsToCreate = 20;
        int updateNthSubmission = 3;
        List<SubmissionEnvelope> submissionEnvelopes = publisher.createManySubmissionsToPublish(numberOfSubmissionsToCreate);
        int publishedCount = 0;
        for (SubmissionEnvelope submissionEnvelope: submissionEnvelopes) {
            publisher.publishASubmisssionEnvelope(submissionEnvelope);

            publishedSubmissions.put(submissionEnvelope.getSubmission().getId(), submissionEnvelope);

            // update every 'Nth' submission after published 5
            if (++publishedCount > 5 && (publishedCount % updateNthSubmission == 0)) {
                SubmissionEnvelope updatedSubmissionEnvelopToPublish = publisher.updateSubmission(getRandomPublishedSubmission());
                publisher.publishASubmisssionEnvelope(updatedSubmissionEnvelopToPublish);
            }
        }
    }

    private SubmissionEnvelope getRandomPublishedSubmission() {
        List<String> publishedSubmissionIds = new ArrayList<>(publishedSubmissions.keySet());

        String randomKey = publishedSubmissionIds.get(ThreadLocalRandom.current().nextInt(0, publishedSubmissionIds.size()));

        return publishedSubmissions.get(randomKey);
    }
}
