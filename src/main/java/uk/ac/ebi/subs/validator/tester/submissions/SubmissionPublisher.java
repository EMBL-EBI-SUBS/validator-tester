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

import java.util.ArrayList;
import java.util.Date;
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

    private static Logger logger = LoggerFactory.getLogger(SubmissionPublisher.class);

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    private Team testTeam;

    @Autowired
    public SubmissionPublisher(RabbitMessagingTemplate rabbitMessagingTemplate, MessageConverter messageConverter) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.rabbitMessagingTemplate.setMessageConverter(messageConverter);

        this.testTeam = Team.build("Test team");
    }

    public SubmissionEnvelope createASubmissionToPublish() {
        return createSubmissionEnvelopes(1).get(0);
    }

    public List<SubmissionEnvelope> createManySubmissionsToPublish(int numberOfSubmissionsToCreate) {
        return createSubmissionEnvelopes(numberOfSubmissionsToCreate);
    }

    public void publishASubmisssionEnvelope(SubmissionEnvelope submissionEnvelope) {
        rabbitMessagingTemplate.convertAndSend(SUBMISSION_EXCHANGE, SUBMISSION_CREATED_ROUTING_KEY, submissionEnvelope);

        logger.debug("Submission id: {} has been published.", submissionEnvelope.getSubmission().getId());
    }

    public SubmissionEnvelope updateSubmission(SubmissionEnvelope randomPublishedSubmissionEnvelope) {
        Submission submissionToUpdate = randomPublishedSubmissionEnvelope.getSubmission();

        submissionToUpdate.setSubmitter(Submitter.build("UPDATED__" + submissionToUpdate.getSubmitter()));

        randomPublishedSubmissionEnvelope.setSubmission(submissionToUpdate);

        logger.debug("Submission id: {} has been updated.", submissionToUpdate.getId());

        return randomPublishedSubmissionEnvelope;
    }

    private List<SubmissionEnvelope> createSubmissionEnvelopes(int count) {
        List<SubmissionEnvelope> submissionEnvelopes = new ArrayList<>();
        List<Submission> submissions = createSubmissions(count);

        for (Submission submission: submissions) {
            List<Sample> samples = createSampleForASubmission();

            SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope(submission);
            submissionEnvelope.setSamples(samples);
            submissionEnvelopes.add(submissionEnvelope);
        }

        return submissionEnvelopes;
    }

    private List<Submission> createSubmissions(int count) {

        List<Submission> submissions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            submissions.add(createSubmission(String.valueOf(i)));
        }

        return submissions;
    }

    private List<Sample> createSampleForASubmission() {
        List<Sample> samples = new ArrayList<>();

        Sample sample = createSample();
        samples.add(sample);

        return samples;
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

    private Submission createSubmission(String id) {
        Submission submission = new Submission();
        submission.setId("testSubmission" + id);
        submission.setSubmissionDate(new Date());
        submission.setSubmitter(Submitter.build("test" + id + "@email.com"));
        submission.setTeam(Team.build("testTeam" + id));

        return submission;
    }
}
