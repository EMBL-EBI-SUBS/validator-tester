# Validator Tester
This project has been designed to test the [validator-prototype](https://github.com/EMBL-EBI-SUBS/validator-prototype), how it handles multiple submissions and updates to submissions still being validated.
The tests will publish Submission

### Run tests
To run the tests you'll have to clone this repository:
```
$ git clone https://github.com/EMBL-EBI-SUBS/validator-tester.git
```
Open the project in your IDE of choice and run the tests under `src/test/java` or use gradle to run them for you.

If you have gradle locally installed: `$ gradle -Dtest.single=<TestClassName> test`

or if you don't have gradle installed: `$ ./gradlew -Dtest.single=<TestClassName> test`

Please don't run all the tests in one go. They are not unit tests, they are specific tests.

#### Test cases

##### Prerequisites:

- You have to start the validator-prototype micro services:
  - validation-coordinator app
  - validators
  - validation-aggregator app
  - validation-status-flipper app

- You also need to have a running RabbitMQ and MongoDB instance.


1. Generate and publish only one submission for validation
<br><br>`$ gradle -Dtest.single=PublishOneSubmissionTest test`

2. Create and publish submissions and update previously published submissions

    You can configure the number of submission to generate and update in the
 `createAndPublishManySubmissionAndRandomlyUpdatesThem` method.
<br><br>`$ gradle -Dtest.single=PublishManySubmissionsTest test`
      
3. Create and publish submissions and update previously published submissions (like case 2.)
and check the validation outcome results in the MongoDB repository
<br><br>`$ gradle -Dtest.single=PublishManySubmissionsTest test`

   You have to wait while the validation is running.
   You can monitor the processing of the messages with the RabbitMQ Manager UI.
   When it is finished you can check the results with executing this test:
   <br><br>`$ gradle -Dtest.single=ValidationOutcomeTest test`
   
   This test reads the latest generated submission result file
   and compare its content with the documents in the MongoDB repository. 
