package uk.ac.ebi.subs.validator.tester.submissions;

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.component.Submitter;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;

import java.util.*;

/**
 *
 * Created by karoly on 10/05/2017.
 */
@Component
public class SubmissionPublisher {

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Autowired
    public SubmissionPublisher(RabbitMessagingTemplate rabbitMessagingTemplate, MessageConverter messageConverter) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.rabbitMessagingTemplate.setMessageConverter(messageConverter);
    }

    public SubmissionEnvelope createASubmissionToPublish() {
        Submission submission = createSubmission("1");

        List<Sample> samples = new ArrayList<>();
        samples.add(new Sample());
        samples.add(new Sample());
        samples.add(new Sample());

        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope(submission);
        submissionEnvelope.setSamples(samples);

        return submissionEnvelope;
    }

    private Submission createSubmission(String id) {
        Submission submission = new Submission();
        submission.setId("testSubmission" + id);
        submission.setSubmissionDate(new Date());
        submission.setSubmitter(Submitter.build("test" + id + "@email.com"));
        submission.setTeam(Team.build("testTeam" + id));

        return submission;
    }

    public void publishASubmisssionEnvelope(SubmissionEnvelope submissionEnvelope) {
        rabbitMessagingTemplate.convertAndSend(
                "usi-1:submission-exchange", "usi.submission.created", submissionEnvelope);
    }
}
