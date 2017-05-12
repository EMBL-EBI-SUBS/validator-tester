package uk.ac.ebi.subs.validator.tester;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.tester.submissions.SubmissionPublisher;

@SpringBootApplication
public class TestingApplication implements CommandLineRunner  {

    @Autowired
    SubmissionPublisher publisher;

    public static void main(String[] args) {
        SpringApplication.run(TestingApplication.class, args);
    }

    @Override
    public void run(String... args) {
        SubmissionEnvelope submissionEnvelope = publisher.createASubmissionToPublish();
        publisher.publishASubmisssionEnvelope(submissionEnvelope);
    }
}
