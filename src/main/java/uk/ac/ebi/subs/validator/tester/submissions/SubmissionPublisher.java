package uk.ac.ebi.subs.validator.tester.submissions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.component.Submitter;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.data.SubmittableValidationEnvelope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is a utility class for publishing submissions to a defined queue.
 * There are 2 methods to publish.
 * The 1st one {@code createASubmissionToPublish} generates and publishes only one submission
 * with a random number of samples between 10 and 100.
 * The 2nd one {@code createManySubmissionsToPublish} generates and publishes a given number of submissions
 * with a random number of samples between 10 and 100.
 *
 * Created by karoly on 10/05/2017.
 */
@Component
public class SubmissionPublisher {

    public static final String SUBMISSION_EXCHANGE = "usi-1:submission-exchange";
    public static final String SUBMISSION_CREATED_ROUTING_KEY = "usi.submission.created";
    public static final String SUBMISSION_UPDATED_ROUTING_KEY = "usi.submission.updated";

    private static Logger logger = LoggerFactory.getLogger(SubmissionPublisher.class);

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    private Team testTeam;

    @Autowired
    public SubmissionPublisher(RabbitMessagingTemplate rabbitMessagingTemplate, MessageConverter messageConverter) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.rabbitMessagingTemplate.setMessageConverter(messageConverter);

        this.testTeam = Team.build("Test team");
    }

    public SubmittableValidationEnvelope createASubmissionToPublish() {
        return createSubmittableEnvelopes(1).get(0);
    }

    public List<SubmittableValidationEnvelope<Sample>> createManySubmissionsToPublish(int numberOfSubmissionsToCreate) {
        return createSubmittableEnvelopes(numberOfSubmissionsToCreate);
    }

    public void publishASubmittableEnvelope(SubmittableValidationEnvelope submittableEnvelope, String routingKey) {
        rabbitMessagingTemplate.convertAndSend(SUBMISSION_EXCHANGE, routingKey, submittableEnvelope);

        logger.debug("Submission id: {} has been published.", submittableEnvelope.getSubmissionId());
    }

    public SubmittableValidationEnvelope<Sample> updateSubmission(String submissionId) {

        SubmittableValidationEnvelope<Sample> submittableValidationEnvelope = createSubmittableSampleValidationEnvelope(submissionId);

        logger.debug("Submission id: {} has been updated.", submissionId);

        return submittableValidationEnvelope;
    }

    private List<SubmittableValidationEnvelope<Sample>> createSubmittableEnvelopes(int count) {
        List<SubmittableValidationEnvelope<Sample>> submittableValidationEnvelopes = new ArrayList<>();
        List<String> submissionIds = createSubmissions(count);

        for (String submissionId: submissionIds) {
            SubmittableValidationEnvelope<Sample> submittableValidationEnvelope = createSubmittableSampleValidationEnvelope(submissionId);
            submittableValidationEnvelopes.add(submittableValidationEnvelope);
        }

        return submittableValidationEnvelopes;
    }

    private SubmittableValidationEnvelope<Sample> createSubmittableSampleValidationEnvelope(String submissionId) {
        Sample sample = createSample();

        return new SubmittableValidationEnvelope<>(submissionId, sample);
    }

    private List<String> createSubmissions(int count) {

        List<String> submissionIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            submissionIds.add("testSubmission" + String.valueOf(i));
        }

        return submissionIds;
    }

    private Sample createSample() {
        Sample sample = new Sample();
        String id = UUID.randomUUID().toString();
        sample.setId("TEST_SAMPLE_" + id);
        sample.setTaxon("testTaxon_" + id);
        sample.setTaxonId(1234L);
        sample.setAccession("ABC_" + id);
        sample.setAlias("TestAlias_" + id);
        sample.setDescription("Description for sample with id: " + id);
        sample.setTeam(testTeam);

        return sample;
    }
}
