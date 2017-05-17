package uk.ac.ebi.subs.validator.tester;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.tester.submissions.SubmissionPublisher;

/**
 * Creates and publish a submission with a random number of samples
 *
 * Created by karoly on 16/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@Category(RabbitMQDependentTest.class)
public class PublishOneSubmissionTest {

    @Autowired
    SubmissionPublisher publisher;

    @Test
    public void createAndPublishASubmission() {
        SubmissionEnvelope submissionEnvelope = publisher.createASubmissionToPublish();
        publisher.publishASubmisssionEnvelope(submissionEnvelope);
    }
}
