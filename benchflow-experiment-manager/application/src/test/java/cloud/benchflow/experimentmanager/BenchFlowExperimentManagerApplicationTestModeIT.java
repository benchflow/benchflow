package cloud.benchflow.experimentmanager;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.helpers.data.BenchFlowData;
import cloud.benchflow.experimentmanager.helpers.data.MinioTestData;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-04
 */
public class BenchFlowExperimentManagerApplicationTestModeIT extends DockerComposeIT {

  private static final int TEST_PORT = 8080;
  private static final String TEST_ADDRESS = "localhost:" + TEST_PORT;

  @Rule
  public final DropwizardAppRule<BenchFlowExperimentManagerConfiguration> RULE =
      new DropwizardAppRule<>(BenchFlowExperimentManagerApplication.class, "../configuration.yml",
          ConfigOverride.config("mongoDB.hostname", MONGO_CONTAINER.getIp()),
          ConfigOverride.config("mongoDB.port", String.valueOf(MONGO_CONTAINER.getExternalPort())),
          ConfigOverride.config("minio.address",
              "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort()),
          ConfigOverride.config("minio.accessKey", MINIO_ACCESS_KEY),
          ConfigOverride.config("minio.secretKey", MINIO_SECRET_KEY),
          ConfigOverride.config("driversMaker.address", TEST_ADDRESS),
          ConfigOverride.config("testManager.address", TEST_ADDRESS),
          ConfigOverride.config("faban.address", TEST_ADDRESS),
          ConfigOverride.config("testMode.mockFaban", "true"));

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(TEST_PORT);

  private MinioService minioServiceSpy;
  private ExecutorService executorService;
  private Client client;


  @Before
  public void setUp() throws Exception {
    minioServiceSpy = Mockito.spy(BenchFlowExperimentManagerApplication.getMinioService());
    BenchFlowExperimentManagerApplication.setMinioService(minioServiceSpy);



    executorService = BenchFlowExperimentManagerApplication.getExperimentTaskScheduler()
        .getExperimentTaskExecutorService();

    client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");
  }

  @Test
  public void runScenarioAlwaysCompleted() throws Exception {

    String experimentID = BenchFlowData.SCENARIO_ALWAYS_COMPLETED_EXPERIMENT_ID;

    runAlwaysCompleteOrDefaultOrFailFirst(experimentID);

  }

  @Test
  public void runNoScenarioSpecified() throws Exception {

    String experimentID = BenchFlowData.NO_SCENARIO_EXPERIMENT_ID;

    runAlwaysCompleteOrDefaultOrFailFirst(experimentID);

  }

  @Test
  public void runScenarioFailFirstExecution() throws Exception {

    String experimentID = BenchFlowData.SCENARIO_FAIL_FIRST_EXEC_EXPERIMENT_ID;

    runAlwaysCompleteOrDefaultOrFailFirst(experimentID);

    // assert that retries is 1
    int numTrials = 2;
    int expectedNumRetries = 1;

    for (int i = 1; i <= numTrials; i++) {
      int numRetries = BenchFlowExperimentManagerApplication.getTrialModelDAO()
          .getNumRetries(experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + i);

      Assert.assertEquals(expectedNumRetries, numRetries);
    }

  }

  private void runAlwaysCompleteOrDefaultOrFailFirst(String experimentID)
      throws FileNotFoundException, InterruptedException,
      BenchFlowExperimentIDDoesNotExistException {

    setUpMocks2Trials(experimentID);

    Response response = client.target(String.format("http://localhost:%d", RULE.getLocalPort()))
        .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
        .path(BenchFlowExperimentResource.RUN_ACTION_PATH).request().post(null);

    Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

    BenchFlowExperimentModelDAO experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient);

    Assert.assertNotNull(experimentModelDAO.getExperimentModel(experimentID));

    // wait long enough for tasks to start to be executed
    executorService.awaitTermination(5, TimeUnit.SECONDS);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));
    Assert.assertEquals(TerminatedState.COMPLETED,
        experimentModelDAO.getTerminatedState(experimentID));

    long expectedExecutedTrials = 2;
    long executedTrials = BenchFlowExperimentManagerApplication.getExperimentModelDAO()
        .getNumExecutedTrials(experimentID);

    Assert.assertEquals(expectedExecutedTrials, executedTrials);
  }

  @Test
  public void runAlwaysFailScenario() throws Exception {

    String experimentID = BenchFlowData.SCENARIO_ALWAYS_FAIL_EXPERIMENT_ID;

    setUpMocks2Trials(experimentID);

    Response response = client.target(String.format("http://localhost:%d", RULE.getLocalPort()))
        .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
        .path(BenchFlowExperimentResource.RUN_ACTION_PATH).request().post(null);

    Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

    BenchFlowExperimentModelDAO experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient);

    Assert.assertNotNull(experimentModelDAO.getExperimentModel(experimentID));

    // wait long enough for tasks to start to be executed
    executorService.awaitTermination(2, TimeUnit.SECONDS);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));
    Assert.assertEquals(TerminatedState.FAILURE,
        experimentModelDAO.getTerminatedState(experimentID));

    long expectedExecutedTrials = 1;
    long executedTrials = BenchFlowExperimentManagerApplication.getExperimentModelDAO()
        .getNumExecutedTrials(experimentID);

    Assert.assertEquals(expectedExecutedTrials, executedTrials);

  }

  @Test
  public void runFailEverySecondExperimentScenario() throws Exception {

    String testID = BenchFlowData.SCENARIO_FAIL_EVERY_SECOND_EXPERIMENT_TEST_ID;

    for (int i = 1; i <= 5; i++) {

      String experimentID = testID + BenchFlowConstants.MODEL_ID_DELIMITER + i;

      setUpMocks2Trials(experimentID);

      Response response = client.target(String.format("http://localhost:%d", RULE.getLocalPort()))
          .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
          .path(BenchFlowExperimentResource.RUN_ACTION_PATH).request().post(null);

      Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

      BenchFlowExperimentModelDAO experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient);

      Assert.assertNotNull(experimentModelDAO.getExperimentModel(experimentID));

      // wait long enough for tasks to start to be executed
      executorService.awaitTermination(2, TimeUnit.SECONDS);

      Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
          experimentModelDAO.getExperimentState(experimentID));

      long expectedExecutedTrials;
      if (i % 2 == 0) {
        Assert.assertEquals(TerminatedState.FAILURE,
            experimentModelDAO.getTerminatedState(experimentID));
        expectedExecutedTrials = 1;
      } else {
        Assert.assertEquals(TerminatedState.COMPLETED,
            experimentModelDAO.getTerminatedState(experimentID));
        expectedExecutedTrials = 2;
      }

      long executedTrials = BenchFlowExperimentManagerApplication.getExperimentModelDAO()
          .getNumExecutedTrials(experimentID);

      Assert.assertEquals(expectedExecutedTrials, executedTrials);

    }

  }

  @Test
  public void abortRunningExperiment() throws Exception {

    String experimentID = BenchFlowData.SCENARIO_ALWAYS_COMPLETED_EXPERIMENT_ID;

    setUpMocks100Trials(experimentID);

    BenchFlowExperimentModelDAO experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient);

    Response response = client.target(String.format("http://localhost:%d", RULE.getLocalPort()))
        .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
        .path(BenchFlowExperimentResource.RUN_ACTION_PATH).request().post(null);

    Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

    Assert.assertNotNull(experimentModelDAO.getExperimentModel(experimentID));

    Response abortResponse =
        client.target(String.format("http://localhost:%d", RULE.getLocalPort()))
            .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
            .path(BenchFlowExperimentResource.ABORT_PATH).request().post(null);

    Assert.assertEquals(Status.NO_CONTENT.getStatusCode(), abortResponse.getStatus());

    // wait long enough for tasks to start to be executed
    executorService.awaitTermination(10, TimeUnit.SECONDS);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));
    Assert.assertEquals(TerminatedState.ABORTED,
        experimentModelDAO.getTerminatedState(experimentID));

  }

  private void setUpMocks2Trials(String experimentID) throws FileNotFoundException {

    int numTrials = 2;

    // make sure experiment has been saved to minio
    minioServiceSpy.saveExperimentDefinition(experimentID,
        MinioTestData.getExperiment2TrialsDefinition());

    setUpOtherMocks(experimentID, numTrials);

  }

  private void setUpMocks100Trials(String experimentID) throws FileNotFoundException {

    int numTrials = 100;

    // make sure experiment has been saved to minio
    minioServiceSpy.saveExperimentDefinition(experimentID,
        MinioTestData.getExperiment100TrialsDefinition());

    setUpOtherMocks(experimentID, numTrials);
  }

  private void setUpOtherMocks(String experimentID, int numTrials) throws FileNotFoundException {

    minioServiceSpy.saveExperimentBPMNModel(experimentID, MinioTestData.BPMN_MODEL_TEST_NAME,
        MinioTestData.getTestModel());
    minioServiceSpy.saveExperimentBPMNModel(experimentID, MinioTestData.BPMN_MODEL_MOCK_NAME,
        MinioTestData.getMockModel());

    minioServiceSpy.saveExperimentDeploymentDescriptor(experimentID,
        MinioTestData.getDeploymentDescriptor());

    // make sure also drivers-maker benchmark is returned
    Mockito.doReturn(MinioTestData.getGeneratedBenchmark()).when(minioServiceSpy)
        .getDriversMakerGeneratedBenchmark(Mockito.anyString(), Mockito.anyLong());

    // make sure also faban configuration file is returned
    Mockito.doReturn(MinioTestData.getFabanConfiguration()).when(minioServiceSpy)
        .getDriversMakerGeneratedFabanConfiguration(Mockito.anyString(), Mockito.anyLong(),
            Mockito.anyLong());

    // Drivers Maker Stub
    stubFor(post(urlEqualTo(DriversMakerService.GENERATE_BENCHMARK_PATH))
        .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode())));

    for (int i = 1; i <= numTrials; i++) {

      String trialID = experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + i;

      // Test Manager Trial Status Stub
      stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromTrialID(trialID)
          + BenchFlowTestManagerService.TRIAL_STATUS_PATH))
              .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())));
    }

    // Test Manager Experiment State Stub
    stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromExperimentID(experimentID)
        + BenchFlowTestManagerService.EXPERIMENT_STATE_PATH))
            .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())));

  }

}
