package cloud.benchflow.testmanager;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.testmanager.constants.BenchFlowConstants.TESTS_PATH;
import static cloud.benchflow.testmanager.constants.BenchFlowConstants.getPathFromExperimentID;
import static cloud.benchflow.testmanager.constants.BenchFlowConstants.getPathFromUsername;

import cloud.benchflow.testmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.testmanager.api.response.RunBenchFlowTestResponse;
import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.helpers.WaitTestCheck;
import cloud.benchflow.testmanager.helpers.WaitTestTermination;
import cloud.benchflow.testmanager.helpers.constants.TestBundle;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState;
import cloud.benchflow.testmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.testmanager.resources.BenchFlowTestResource;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.scheduler.running.RunningStatesHandler;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-06
 */
public class TestManagerSmokeTestsIT extends DockerComposeIT {

  /**
   * Tests that ensure that given tests can be submitted successfully. TODO - add integration with
   * experiment-manager to check complete execution or TODO - mocking of experiment-manager
   */

  private static String TEST_USERNAME = "smokeTestUser";

  @Rule
  public final DropwizardAppRule<BenchFlowTestManagerConfiguration> RULE =
      new DropwizardAppRule<>(BenchFlowTestManagerApplication.class, "../configuration.yml",
          ConfigOverride.config("mongoDB.hostname", MONGO_CONTAINER.getIp()),
          ConfigOverride.config("mongoDB.port", String.valueOf(MONGO_CONTAINER.getExternalPort())),
          ConfigOverride.config("minio.address",
              "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort()),
          ConfigOverride.config("minio.accessKey", MINIO_ACCESS_KEY),
          ConfigOverride.config("minio.secretKey", MINIO_SECRET_KEY),
          ConfigOverride.config("benchFlowExperimentManager.address", "localhost"));

  // needs to be subfolder of the target folder for Wercker
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));

  private Client client;
  private BenchFlowExperimentManagerService benchFlowExperimentManagerServiceSpy;
  private BenchFlowTestModelDAO testModelDAO;
  private TestTaskScheduler testTaskSchedulerSpy;
  private RunningStatesHandler runningStatesHandlerSpy;

  @Before
  public void setUp() throws Exception {
    // needed for multipart client
    // https://github.com/dropwizard/dropwizard/issues/1013
    JerseyClientConfiguration configuration = new JerseyClientConfiguration();
    configuration.setChunkedEncodingEnabled(false);
    // needed because parsing testYaml takes more than default time
    configuration.setTimeout(Duration.milliseconds(5000));

    client =
        new JerseyClientBuilder(RULE.getEnvironment()).using(configuration).build("test client");

    benchFlowExperimentManagerServiceSpy =
        Mockito.spy(BenchFlowTestManagerApplication.getExperimentManagerService());
    BenchFlowTestManagerApplication
        .setExperimentManagerService(benchFlowExperimentManagerServiceSpy);

    testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();

    // setup TestTaskScheduler for spy
    testTaskSchedulerSpy = Mockito.spy(BenchFlowTestManagerApplication.getTestTaskScheduler());
    BenchFlowTestManagerApplication.setTestTaskScheduler(testTaskSchedulerSpy);

    testTaskSchedulerSpy.getTestDispatcher().setTaskScheduler(testTaskSchedulerSpy);

    runningStatesHandlerSpy = Mockito.spy(testTaskSchedulerSpy.getRunningStatesHandler());
    testTaskSchedulerSpy.setRunningStatesHandler(runningStatesHandlerSpy);

    BenchFlowTestManagerApplication.getExperimentResource()
        .setTestTaskScheduler(testTaskSchedulerSpy);

  }

  private String getExpectedTestID(String testName) {
    return TEST_USERNAME + MODEL_ID_DELIMITER + testName + MODEL_ID_DELIMITER + 1;
  }

  @Test
  public void runLoadTest() throws Exception {

    String testName = "WfMSLoadTest";
    int expectedNumExperiments = 1;

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getLoadTestBundleFile(temporaryFolder), MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart, expectedNumExperiments);

  }

  @Test
  public void runExplorationExhaustiveOneAtATimeUsersTest() throws Exception {

    String testName = "WfMSExhaustiveExplorationOneAtATimeSelectionUsersTest";
    int expectedNumExperiments = 4;

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestExplorationOneAtATimeUsersBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart, expectedNumExperiments);

  }

  @Test
  public void runExplorationExhaustiveOneAtATimeMemoryTest() throws Exception {

    String testName = "WfMSExhaustiveExplorationOneAtATimeSelectionMemoryTest";
    int expectedNumExperiments = 2;

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestExplorationOneAtATimeMemoryBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart, expectedNumExperiments);

  }

  @Test
  public void runTestExplorationRandomUsersTest() throws Exception {

    String testName = "WfMSExhaustiveExplorationRandomSelectionUsersTest";
    int expectedNumExperiments = 4;

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestExplorationRandomUsersBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart, expectedNumExperiments);

  }

  @Test
  public void runTestExplorationExhaustiveOneAtATimeUsersEnvironmentTest() throws Exception {

    String testName = "WfMSExhaustiveExplorationOneAtATimeSelectionUsersEnvironmentTest";
    int expectedNumExperiments = 6;

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestExplorationOneAtATimeUsersEnvironmentBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart, expectedNumExperiments);


  }

  @Test
  public void runTestTerminationCriteria() throws Exception {

    String testName = "TestTerminationCriteriaTest";
    int expectedNumExperiments = 1;

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestTerminationCriteriaBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart, expectedNumExperiments);


  }

  @Test
  public void runTestStepUsers() throws Exception {

    String testName = "WfMSStepUsersExhaustiveExplorationTest";
    int expectedNumExperiments = 4;

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getTestStepUsersBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    runTest(testName, fileDataBodyPart, expectedNumExperiments);

  }

  private void runTest(String testName, FileDataBodyPart fileDataBodyPart,
      int expectedNumExperiments)
      throws BenchFlowTestIDDoesNotExistException, InterruptedException {

    String expectedTestID = getExpectedTestID(testName);

    // setup experiment manager mock and send experiment terminated
    BenchFlowExperimentStateRequest experimentStateRequest = new BenchFlowExperimentStateRequest(
        BenchFlowExperimentState.TERMINATED, TerminatedState.COMPLETED);

    Mockito.doAnswer(invocationOnMock -> {

      new Thread(() -> {

        // sleep to simulate execution on experiment manager
        try {

          Thread.sleep(1000);

          String experimentID = invocationOnMock.getArgument(0);

          client.target(String.format("http://localhost:%d/", RULE.getLocalPort()))
              .path(getPathFromExperimentID(experimentID))
              .path(BenchFlowExperimentResource.STATE_PATH).request()
              .put(Entity.entity(experimentStateRequest, MediaType.APPLICATION_JSON));

        } catch (InterruptedException e) {
          e.printStackTrace();
        }


      }).start();

      return null;

    }).when(benchFlowExperimentManagerServiceSpy).runBenchFlowExperiment(Matchers.anyString());

    // setup the client and submit test

    MultiPart multiPart = new MultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
    multiPart.bodyPart(fileDataBodyPart);

    Response response = client.target(String.format("http://localhost:%d/", RULE.getLocalPort()))
        .path(getPathFromUsername(TEST_USERNAME)).path(TESTS_PATH)
        .path(BenchFlowTestResource.RUN_PATH).register(MultiPartFeature.class)
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(multiPart, multiPart.getMediaType()));

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    RunBenchFlowTestResponse testResponse = response.readEntity(RunBenchFlowTestResponse.class);
    Assert.assertEquals(expectedTestID, testResponse.getTestID());

    // wait until test terminates
    // check when the test reaches the final state, with a timeout
    long timeout = 30 * 1000; // 30 seconds

    WaitTestCheck waitTestCheck = () -> {

      // wait for last task to finish
      BenchFlowTestManagerApplication.getTestTaskScheduler().getTaskExecutorService()
          .awaitTermination(10, TimeUnit.SECONDS);

    };

    WaitTestTermination.waitForTestTerminationWithTimeout(expectedTestID, testModelDAO,
        waitTestCheck, timeout);

    // assert test was terminated
    BenchFlowTestState testState = testModelDAO.getTestState(expectedTestID);

    Assert.assertEquals(BenchFlowTestState.TERMINATED, testState);

    Mockito.verify(testTaskSchedulerSpy, Mockito.times(1)).handleTerminatedState(expectedTestID);

    // assert all test were executed
    Set<Long> experimentNumbers = testModelDAO.getExperimentNumbers(getExpectedTestID(testName));
    for (long i = 1; i <= expectedNumExperiments; i++) {
      Assert.assertTrue(experimentNumbers.contains(i));
    }

  }
}
