package uk.ac.ebi.subs.validator.tester;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationStatus;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;
import uk.ac.ebi.subs.validator.tester.utils.ValidationResultProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by karoly on 24/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@EnableMongoRepositories(basePackageClasses = ValidationResultRepository.class)
@EnableAutoConfiguration
@Category(RabbitMQDependentTest.class)
public class ValidationResultTest {

    private static final Logger logger = LoggerFactory.getLogger(ValidationResultTest.class);

    private Path LAST_GENERATED_FILE_PATH = Paths.get("src/test/resources/generated/lastGeneratedFile.txt");

    private Map<String, ValidationResultProperties> submissionsToCheck = new LinkedHashMap<>();

    @Autowired
    private ValidationResultRepository repository;

    @Test
    public void testValidationResults() throws IOException {
        readSubmissionsResultFile();

        submissionsToCheck.forEach((submissionId, properties) -> {

            logger.debug("Testing submission id: {}, validation result properties: {}", submissionId, properties);

            List<ValidationResult> validationResults =
                    repository.findBySubmissionIdAndEntityUuid(submissionId, properties.getEntityUuid());

            assertThat(validationResults.size(), is(1));

            ValidationResult validationResult = validationResults.get(0);

            assertTrue("The version of the validation result is not correct",
                    validationResult.getVersion() == properties.getVersion());
            ValidationStatus validationState = validationResult.getValidationStatus();
            assertTrue("The state of the validation result is not Complete, currently it is "
                            + validationState.toString(),
                    validationState == ValidationStatus.Complete);
        });
    }

    private void readSubmissionsResultFile() throws IOException {
        File submissionsResultFile = new File(getLatestGeneratedFilePath());

        ObjectMapper mapper = new ObjectMapper();
        submissionsToCheck = mapper.readValue(submissionsResultFile,
                                                new TypeReference<Map<String, ValidationResultProperties>>() {});
    }

    private String getLatestGeneratedFilePath() throws IOException {
        return Files.readAllLines(LAST_GENERATED_FILE_PATH).get(0);
    }
}
