package uk.ac.ebi.subs.validator.tester;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.validator.data.ValidationOutcome;
import uk.ac.ebi.subs.validator.data.ValidationOutcomeEnum;
import uk.ac.ebi.subs.validator.flipper.OutcomeDocumentService;
import uk.ac.ebi.subs.validator.repository.ValidationOutcomeRepository;
import uk.ac.ebi.subs.validator.tester.utils.ValidationOutcomeProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Created by karoly on 24/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@EnableMongoRepositories(basePackageClasses = ValidationOutcomeRepository.class)
@EnableAutoConfiguration
@Category(RabbitMQDependentTest.class)
public class ValidationOutcomeTest {

    private static final Logger logger = LoggerFactory.getLogger(OutcomeDocumentService.class);

    private Path LAST_GENERATED_FILE_PATH = Paths.get("src/test/resources/generated/lastGeneratedFile.txt");

    private Map<String, ValidationOutcomeProperties> submissionsToCheck = new LinkedHashMap<>();

    @Autowired
    private ValidationOutcomeRepository repository;

    @Test
    public void testValidationOutcomes() throws IOException {
        readSubmissionsResultFile();

        submissionsToCheck.forEach((submissionId, properties) -> {

            logger.debug("Testing submission id: {}, validationOutcome properties: {}", submissionId, properties);

            List<ValidationOutcome> validationOutcomeDocuments =
                    repository.findBySubmissionIdAndEntityUuid(submissionId, properties.getEntityUuid());

            assertThat(validationOutcomeDocuments.size(), is(1));

            ValidationOutcome validationOutcomeDocument = validationOutcomeDocuments.get(0);

            assertTrue("The version of the validation outcome is not correct",
                    validationOutcomeDocument.getVersion() == properties.getVersion());
            ValidationOutcomeEnum validationState = validationOutcomeDocument.getValidationOutcome();
            assertTrue("The state of the validation outcome is not Complete, currently it is "
                            + validationState.toString(),
                    validationState == ValidationOutcomeEnum.Complete);
        });
    }

    private void readSubmissionsResultFile() throws IOException {
        File submissionsResultFile = new File(getLatestGeneratedFilePath());

        ObjectMapper mapper = new ObjectMapper();
        submissionsToCheck = mapper.readValue(submissionsResultFile,
                                                new TypeReference<Map<String, ValidationOutcomeProperties>>() {});
    }

    private String getLatestGeneratedFilePath() throws IOException {
        return Files.readAllLines(LAST_GENERATED_FILE_PATH).get(0);
    }
}
