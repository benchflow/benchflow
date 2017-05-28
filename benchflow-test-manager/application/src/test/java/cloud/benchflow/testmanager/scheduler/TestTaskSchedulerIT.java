package cloud.benchflow.testmanager.scheduler;

import static cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState.TERMINATED;
import static cloud.benchflow.testmanager.models.BenchFlowExperimentModel.TerminatedState.COMPLETED;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.DockerComposeIT;
import cloud.benchflow.testmanager.archive.TestArchives;
import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.helpers.TestConstants;
import cloud.benchflow.testmanager.helpers.TestFiles;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import cloud.benchflow.testmanager.tasks.start.StartTask;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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

  private TestTaskScheduler testTaskController;
  private BenchFlowTestModelDAO testModelDAO;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private UserDAO userDAO;
  private BenchFlowExperimentManagerService experimentManagerService;
  private ExecutorService executorService;

  @Before
  public void setUp() throws Exception {

    testTaskController = BenchFlowTestManagerApplication.getTestTaskController();
    executorService = testTaskController.getTaskExecutorService();

    userDAO = BenchFlowTestManagerApplication.getUserDAO();
    testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
    experimentModelDAO = BenchFlowTestManagerApplication.getExperimentModelDAO();
    experimentManagerService =
        Mockito.spy(BenchFlowTestManagerApplication.getExperimentManagerService());
    BenchFlowTestManagerApplication.setExperimentManagerService(experimentManagerService);
  }

  @Test
  public void runCompleteExploration() throws Exception {

    /*
     Since we have many asynchronous tasks running we set a countdown
     latch to count down to the expected number of experiments.
     If something does not work we have a max waiting time.
    */

    String testName = "WfMSTest";
    int expectedExperiments = 4;

    CountDownLatch countDownLatch = new CountDownLatch(expectedExperiments);

    Mockito.doAnswer(invocationOnMock -> {
      String experimentID = (String) invocationOnMock.getArguments()[0];

      experimentModelDAO.setExperimentState(experimentID, TERMINATED, null, COMPLETED);

      String testID = BenchFlowConstants.getTestIDFromExperimentID(experimentID);
      testTaskController.handleTestState(testID);

      countDownLatch.countDown();

      return null;
    }).when(experimentManagerService).runBenchFlowExperiment(Matchers.anyString());

    User user = userDAO.addUser(TestConstants.TEST_USER_NAME);

    String testID = testModelDAO.addTestModel(testName, user);

    String testDefinitionString = IOUtils
        .toString(TestFiles.getTestExplorationCompleteUsersInputStream(), StandardCharsets.UTF_8);
    InputStream deploymentDescriptorInputStream =
        TestArchives.getValidDeploymentDescriptorInputStream();
    Map<String, InputStream> bpmnModelsInputStream = TestArchives.getValidBPMNModels();

    Thread startTaskThread = new Thread(new StartTask(testID, testDefinitionString,
        deploymentDescriptorInputStream, bpmnModelsInputStream));

    startTaskThread.start();

    startTaskThread.join();

    countDownLatch.await(120, TimeUnit.SECONDS);

    // wait for last task to finish
    executorService.awaitTermination(1, TimeUnit.SECONDS);

    // assert that all experiments have been executed
    Assert.assertEquals(0, countDownLatch.getCount());

    // asssert that test has been set as TERMINATED
    Assert.assertEquals(BenchFlowTestModel.BenchFlowTestState.TERMINATED,
        testModelDAO.getTestState(testID));
  }
}
