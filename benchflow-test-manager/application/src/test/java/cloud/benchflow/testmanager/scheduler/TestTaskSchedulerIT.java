package cloud.benchflow.testmanager.scheduler;

import static cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState.RUNNING;
import static cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState.TERMINATED;
import static cloud.benchflow.testmanager.models.BenchFlowExperimentModel.TerminatedState.COMPLETED;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.DockerComposeIT;
import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.helpers.WaitTestCheck;
import cloud.benchflow.testmanager.helpers.WaitTestTermination;
import cloud.benchflow.testmanager.helpers.constants.TestBundle;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.helpers.constants.TestFiles;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestTerminatedState;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.models.explorationspace.MongoCompatibleExplorationSpace;
import cloud.benchflow.testmanager.scheduler.running.RunningStatesHandler;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Path;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-27
 */
public class TestTaskSchedulerIT extends DockerComposeIT {

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

  // needs to be subfolder of current folder for Wercker
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));

  private TestTaskScheduler testTaskScheduler;
  private RunningStatesHandler notMockedRunningStatesHandler;
  private RunningStatesHandler runningStatesHandler;
  private BenchFlowTestModelDAO testModelDAO;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private ExplorationModelDAO explorationModelDAO;
  private UserDAO userDAO;
  private MinioService minioService;
  private BenchFlowExperimentManagerService experimentManagerService;
  private CustomFutureReturningExecutor executorService;
  private User user;

  @Before
  public void setUp() throws Exception {

    testTaskScheduler = BenchFlowTestManagerApplication.getTestTaskScheduler();
    notMockedRunningStatesHandler = testTaskScheduler.getRunningStatesHandler();
    testTaskScheduler
        .setRunningStatesHandler(Mockito.spy(testTaskScheduler.getRunningStatesHandler()));
    runningStatesHandler = testTaskScheduler.getRunningStatesHandler();
    executorService = testTaskScheduler.getTaskExecutorService();

    userDAO = BenchFlowTestManagerApplication.getUserDAO();
    testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
    experimentModelDAO = BenchFlowTestManagerApplication.getExperimentModelDAO();
    explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    minioService = BenchFlowTestManagerApplication.getMinioService();
    BenchFlowTestManagerApplication.setExperimentManagerService(
        Mockito.spy(BenchFlowTestManagerApplication.getExperimentManagerService()));
    experimentManagerService = BenchFlowTestManagerApplication.getExperimentManagerService();

    user = userDAO.addUser(TestConstants.TEST_USER_NAME);

  }

  @After
  public void tearDown() throws Exception {
    userDAO.removeUser(user.getUsername());
  }

  @Test
  public void runOneAtATimeExplorationUsers() throws Exception {

    String testName = "WfMSTest";
    int expectedNumExperiments = 4;

    String testID = testModelDAO.addTestModel(testName, user);

    String testDefinitionString = IOUtils
        .toString(TestFiles.getTestExplorationOneAtATimeUsersInputStream(), StandardCharsets.UTF_8);

    CountDownLatch countDownLatch =
        setUpExplorationMocks(testID, expectedNumExperiments, testDefinitionString);

    // handle in scheduler
    testTaskScheduler.handleStartingTest(testID);

    // check when the test reaches the final state, with a timeout
    long timeout = 3 * 60 * 1000; //3 minutes

    WaitTestCheck waitTestCheck = () -> {
      // wait long enough for all experiments to complete
      countDownLatch.await(10, TimeUnit.SECONDS);

      // wait for last task to finish
      executorService.awaitTermination(1, TimeUnit.SECONDS);
    };

    WaitTestTermination.waitForTestTerminationWithTimeout(testID, testModelDAO, waitTestCheck,
        timeout);

    // assert that all experiments have been executed
    Assert.assertEquals(0, countDownLatch.getCount());

    // assert that test has been set as TERMINATED
    Assert.assertEquals(BenchFlowTestModel.BenchFlowTestState.TERMINATED,
        testModelDAO.getTestState(testID));

    // assert that the complete exploration space has been execution
    List<Integer> expectedIndices = new ArrayList<>();
    for (int i = 0; i < expectedNumExperiments; i++) {
      expectedIndices.add(i);
    }

    Assert.assertEquals(expectedIndices,
        explorationModelDAO.getExecutedExplorationPointIndices(testID));

    // assert that exploration space has been saved in DB
    MongoCompatibleExplorationSpace explorationSpace =
        explorationModelDAO.getExplorationSpace(testID);

    Assert.assertEquals(expectedNumExperiments, explorationSpace.getSize());

  }

  @Test
  public void runRandomBreakdownExplorationUsers() throws Exception {

    String testName = "WfMSTest";
    int expectedNumExperiments = 4;

    String testID = testModelDAO.addTestModel(testName, user);

    String testDefinitionString = IOUtils
        .toString(TestFiles.getTestExplorationRandomUsersInputStream(), StandardCharsets.UTF_8);

    CountDownLatch countDownLatch =
        setUpExplorationMocks(testID, expectedNumExperiments, testDefinitionString);

    // handle in scheduler
    testTaskScheduler.handleStartingTest(testID);

    // check when the test reaches the final state, with a timeout
    long timeout = 1 * 60 * 1000; //3 minutes

    WaitTestCheck waitTestCheck = () -> {
      // wait long enough for all experiments to complete
      countDownLatch.await(10, TimeUnit.SECONDS);
    };

    WaitTestTermination.waitForTestTerminationWithTimeout(testID, testModelDAO, waitTestCheck,
        timeout);

    // wait for last task to finish
    executorService.awaitTermination(1, TimeUnit.SECONDS);

    // assert that all experiments have been executed
    Assert.assertEquals(0, countDownLatch.getCount());

    // assert that test has been set as TERMINATED
    Assert.assertEquals(BenchFlowTestModel.BenchFlowTestState.TERMINATED,
        testModelDAO.getTestState(testID));

    // assert that the complete exploration space has been execution
    List<Integer> explorationPointIndices =
        explorationModelDAO.getExecutedExplorationPointIndices(testID);

    Assert.assertEquals(expectedNumExperiments, explorationPointIndices.size());

    // sort the list and check that all expected values are there
    List<Integer> sortedIndices = new ArrayList<>();
    for (int i = 0; i < expectedNumExperiments; i++) {
      sortedIndices.add(i);
    }

    explorationPointIndices.sort(Integer::compareTo);
    Assert.assertEquals(sortedIndices, explorationPointIndices);

  }

  @Test
  public void runOneAtATimeExplorationUsersMemoryEnvironment() throws Exception {

    String testName = "WfMSTest";
    int expectedNumExperiments = 16;

    String testID = testModelDAO.addTestModel(testName, user);

    String testDefinitionString =
        IOUtils.toString(TestFiles.getTestExplorationOneAtATimeUsersMemoryEnvironmentInputStream(),
            StandardCharsets.UTF_8);

    CountDownLatch countDownLatch =
        setUpExplorationMocks(testID, expectedNumExperiments, testDefinitionString);

    // handle in scheduler
    testTaskScheduler.handleStartingTest(testID);

    // check when the test reaches the final state, with a timeout
    long timeout = 2 * 60 * 1000; //3 minutes

    WaitTestCheck waitTestCheck = () -> {
      // wait long enough for all experiments to complete
      countDownLatch.await(40, TimeUnit.SECONDS);

      // wait for last task to finish
      executorService.awaitTermination(10, TimeUnit.SECONDS);
    };

    WaitTestTermination.waitForTestTerminationWithTimeout(testID, testModelDAO, waitTestCheck,
        timeout);

    // assert that all experiments have been executed
    Assert.assertEquals(0, countDownLatch.getCount());

    // assert that test has been set as TERMINATED
    Assert.assertEquals(BenchFlowTestModel.BenchFlowTestState.TERMINATED,
        testModelDAO.getTestState(testID));

    // assert that the complete exploration space has been execution
    List<Integer> expectedIndices = new ArrayList<>();
    for (int i = 0; i < expectedNumExperiments; i++) {
      expectedIndices.add(i);
    }

    Assert.assertEquals(expectedIndices,
        explorationModelDAO.getExecutedExplorationPointIndices(testID));

    // assert that deployment descriptor has changed
    String deploymentDescriptor1 = IOUtils.toString(minioService.getExperimentDeploymentDescriptor(
        testID + BenchFlowConstants.MODEL_ID_DELIMITER + 1), StandardCharsets.UTF_8);
    // assert memory limit added
    Assert.assertTrue(deploymentDescriptor1.contains("mem_limit: 500m"));

    // assert environment changed
    Assert.assertTrue(deploymentDescriptor1.contains("SIZE_OF_THREADPOOL=1"));
    Assert.assertTrue(deploymentDescriptor1.contains("AN_ENUM=A"));


  }

  @Test
  public void runLoadTest() throws Exception {

    String testName = TestConstants.LOAD_TEST_NAME;
    int expectedNumExperiments = 1;

    String testID = testModelDAO.addTestModel(testName, user);

    String testDefinitionString =
        IOUtils.toString(TestFiles.getTestLoadInputStream(), StandardCharsets.UTF_8);

    CountDownLatch countDownLatch =
        setUpExplorationMocks(testID, expectedNumExperiments, testDefinitionString);

    // handle in scheduler
    testTaskScheduler.handleStartingTest(testID);

    // check when the test reaches the final state, with a timeout
    long timeout = 2 * 60 * 1000; //2 minutes

    WaitTestCheck waitTestCheck = () -> {
      // wait long enough for all experiments to complete
      countDownLatch.await(10, TimeUnit.SECONDS);

      // wait for last task to finish
      executorService.awaitTermination(1, TimeUnit.SECONDS);
    };

    WaitTestTermination.waitForTestTerminationWithTimeout(testID, testModelDAO, waitTestCheck,
        timeout);

    // assert that all experiments have been executed
    Assert.assertEquals(0, countDownLatch.getCount());

    // assert that test has been set as TERMINATED
    Assert.assertEquals(BenchFlowTestModel.BenchFlowTestState.TERMINATED,
        testModelDAO.getTestState(testID));

  }

  @Test
  public void runBenchFlowTestTimeoutTestBeforeRunningExperiments() throws Exception {

    String testName = TestConstants.TEST_TERMINATION_CRITERIA_NAME;

    String testID = testModelDAO.addTestModel(testName, user);

    String testDefinitionString =
        IOUtils.toString(TestFiles.getTestTerminationCriteriaInputStream(), StandardCharsets.UTF_8);

    // Wait so the Timeout Triggers in after DETERMINE_EXPLORATION_STRATEGY state
    Mockito.doAnswer(invocationOnMock -> {

      String testIDCalledMethod = (String) invocationOnMock.getArguments()[0];

      // Execute the real method
      notMockedRunningStatesHandler.determineExplorationStrategy(testIDCalledMethod);

      try {
        long timeout = testModelDAO.getMaxRunningTime(testIDCalledMethod).toMillisPart();
        Thread.sleep(timeout);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      return null;
    }).when(runningStatesHandler).determineExplorationStrategy(Matchers.anyString());

    // Wait in ADD_STORED_KNOWLEDGE state
    Mockito.doAnswer(invocationOnMock -> {

      String testIDCalledMethod = (String) invocationOnMock.getArguments()[0];

      try {
        long timeout = testModelDAO.getMaxRunningTime(testIDCalledMethod).toMillisPart();
        Thread.sleep(timeout);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      return null;
    }).when(runningStatesHandler).addStoredKnowledge(Matchers.anyString());

    setupTestOnMinio(testID, testDefinitionString);

    // handle in scheduler
    testTaskScheduler.handleStartingTest(testID);

    // check when the test reaches the final state, with a timeout
    long timeout = 2 * 60 * 1000; //2 minutes

    WaitTestCheck waitTestCheck = () -> {

      // wait for last task to finish
      executorService.awaitTermination(10, TimeUnit.SECONDS);

    };

    WaitTestTermination.waitForTestTerminationWithTimeout(testID, testModelDAO, waitTestCheck,
        timeout);

    // assert that test has been set as TERMINATED
    Assert.assertEquals(BenchFlowTestModel.BenchFlowTestState.TERMINATED,
        testModelDAO.getTestState(testID));

    // assert that test has been set as PARTIALLY_COMPLETE
    Assert.assertEquals(TestTerminatedState.PARTIALLY_COMPLETE,
        testModelDAO.getTestTerminatedState(testID));

  }

  @Test
  public void runBenchFlowTestTimeoutTestWhenRunningExperiments() throws Exception {

    String testName = TestConstants.TEST_TERMINATION_CRITERIA_NAME;

    String testID = testModelDAO.addTestModel(testName, user);

    int expectedNumExperiments = 1;

    String testDefinitionString =
        IOUtils.toString(TestFiles.getTestTerminationCriteriaInputStream(), StandardCharsets.UTF_8);

    // set the experiment state as running
    Mockito.doAnswer(invocationOnMock -> {
      String experimentID = (String) invocationOnMock.getArguments()[0];

      experimentModelDAO.setExperimentState(experimentID, RUNNING,
          RunningState.DETERMINE_EXECUTE_TRIALS, null);

      return null;
    }).when(experimentManagerService).runBenchFlowExperiment(Matchers.anyString());

    // set the experiment state as terminated when an abort is triggered
    Mockito.doAnswer(invocationOnMock -> {
      String experimentID = (String) invocationOnMock.getArguments()[0];

      experimentModelDAO.setExperimentState(experimentID, TERMINATED, null, COMPLETED);

      return null;
    }).when(experimentManagerService).abortBenchFlowExperiment(Matchers.anyString());

    Mockito.doAnswer(invocationOnMock -> {

      String testIDCalledMethod = (String) invocationOnMock.getArguments()[0];

      try {
        long timeout = testModelDAO.getMaxRunningTime(testIDCalledMethod).toMillisPart();
        Thread.sleep(timeout + 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      return null;
    }).when(runningStatesHandler).handleExperimentResult(Matchers.anyString());

    setupTestOnMinio(testID, testDefinitionString);

    // handle in scheduler
    testTaskScheduler.handleStartingTest(testID);

    // check when the test reaches the final state, with a timeout
    long timeout = 2 * 60 * 1000; //3 minutes

    WaitTestCheck waitTestCheck = () -> {

      // wait for last task to finish
      executorService.awaitTermination(10, TimeUnit.SECONDS);

    };

    WaitTestTermination.waitForTestTerminationWithTimeout(testID, testModelDAO, waitTestCheck,
        timeout);

    // assert that test has been set as TERMINATED
    Assert.assertEquals(BenchFlowTestModel.BenchFlowTestState.TERMINATED,
        testModelDAO.getTestState(testID));

    // assert that test has been set as PARTIALLY_COMPLETE
    Assert.assertEquals(TestTerminatedState.PARTIALLY_COMPLETE,
        testModelDAO.getTestTerminatedState(testID));

    // assert that the scheduled experiment is TERMINATED
    Collection<BenchFlowExperimentModel> experiments =
        testModelDAO.getTestModel(testID).getExperimentModels();
    BenchFlowExperimentModel[] experimentsArray =
        experiments.toArray(new BenchFlowExperimentModel[experiments.size()]);

    Assert.assertEquals(TERMINATED, experimentsArray[expectedNumExperiments - 1].getState());

    // assert that test was removed from experiment-manager
    Mockito.verify(experimentManagerService, Mockito.times(1))
        .abortBenchFlowExperiment(Matchers.anyString());

  }

  private CountDownLatch setUpExplorationMocks(String testID, int expectedNumExperiments,
      String testDefinitionString) throws IOException {

    /*
     Since we have many asynchronous tasks running we set a countdown
     latch to count down to the expected number of experiments.
     If something does not work we have a max waiting time.
    */

    CountDownLatch countDownLatch = new CountDownLatch(expectedNumExperiments);

    Mockito.doAnswer(invocationOnMock -> {
      String experimentID = (String) invocationOnMock.getArguments()[0];

      experimentModelDAO.setExperimentState(experimentID, TERMINATED, null, COMPLETED);

      new Thread(() -> {

        // TODO - try to find a way to deterministically execute some code, after a given mocked method is called.
        try {
          Thread.sleep(2000);
          testTaskScheduler.handleRunningTest(testID);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }).start();

      countDownLatch.countDown();

      return null;
    }).when(experimentManagerService).runBenchFlowExperiment(Matchers.anyString());

    setupTestOnMinio(testID, testDefinitionString);

    return countDownLatch;

  }

  private void setupTestOnMinio(String testID, String testDefinitionString) throws IOException {
    InputStream deploymentDescriptorInputStream =
        TestBundle.getValidDeploymentDescriptorInputStream();
    Map<String, InputStream> bpmnModelInputStreams = TestBundle.getValidBPMNModels();

    // extract contents
    InputStream definitionInputStream =
        IOUtils.toInputStream(testDefinitionString, StandardCharsets.UTF_8);

    // save Test Bundle contents to Minio
    minioService.saveTestDefinition(testID, definitionInputStream);
    minioService.saveTestDeploymentDescriptor(testID, deploymentDescriptorInputStream);

    bpmnModelInputStreams.forEach(
        (fileName, inputStream) -> minioService.saveTestBPMNModel(testID, fileName, inputStream));
  }

  // Not possible because the Timeout is set when the state is RUNNING, but
  // here because probably useful when a user can trigger an abort
  //  @Test
  //  public void runBenchFlowTestTimeoutTestWhenTestStart() throws Exception {
  //
  //    String testName = TestConstants.TEST_TERMINATION_CRITERIA_NAME;
  //
  //    String testID = testModelDAO.addTestModel(testName, user);
  //
  //    String testDefinitionString =
  //        IOUtils.toString(TestFiles.getTestTerminationCriteriaInputStream(), StandardCharsets.UTF_8);
  //
  //    // Wait so the Timeout Triggers in the START state
  //    Mockito.doAnswer(invocationOnMock -> {
  //
  //      try {
  //        Thread.sleep(5000);
  //      } catch (InterruptedException e) {
  //        e.printStackTrace();
  //      }
  //
  //      return null;
  //    }).doCallRealMethod().when(testTaskScheduler).handleStartState(Matchers.anyString());
  //
  //    setupTestOnMinio(testID, testDefinitionString);
  //
  //    // handle in scheduler
  //    testTaskScheduler.handleStartingTest(testID);
  //
  //    // check when the test reaches the final state, with a timeout
  //    long timeout = 1 * 60 * 1000; //3 minutes
  //
  //    WaitTestCheck waitTestCheck = () -> {
  //
  //      // wait for last task to finish
  //      executorService.awaitTermination(20, TimeUnit.SECONDS);
  //
  //    };
  //
  //    WaitTestTermination.waitForTestTerminationWithTimeout(testID, testModelDAO, waitTestCheck, timeout);
  //
  //    // assert that test has been set as TERMINATED
  //    Assert.assertEquals(BenchFlowTestModel.BenchFlowTestState.TERMINATED,
  //        testModelDAO.getTestState(testID));
  //
  //    // assert that test has been set as PARTIALLY_COMPLETE
  //    Assert.assertEquals(TestTerminatedState.PARTIALLY_COMPLETE,
  //        testModelDAO.getTestTerminatedState(testID));
  //
  //  }
}
