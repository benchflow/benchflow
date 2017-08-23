package cloud.benchflow.experimentmanager;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import cloud.benchflow.experimentmanager.api.request.FabanStatusRequest;
import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants.TrialIDElements;
import cloud.benchflow.experimentmanager.helpers.data.BenchFlowData;
import cloud.benchflow.experimentmanager.helpers.data.MinioTestData;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.experimentmanager.resources.TrialResource;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.responses.DeployStatus;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-13
 */
public class BenchFlowExperimentManagerApplicationIT extends DockerComposeIT {

  private static final int TEST_PORT = 8085;
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
          ConfigOverride.config("faban.address", TEST_ADDRESS));

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(TEST_PORT);

  private MinioService minioServiceSpy;
  private FabanClient fabanClientMock = Mockito.mock(FabanClient.class);
  private ExecutorService executorService;

  private String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_1_TRIAL;
  private FabanManagerService fabanManagerServiceSpy;

  @Before
  public void setUp() throws Exception {

    minioServiceSpy = Mockito.spy(BenchFlowExperimentManagerApplication.getMinioService());

    BenchFlowExperimentManagerApplication.setMinioService(minioServiceSpy);

    fabanManagerServiceSpy =
        Mockito.spy(new FabanManagerService(fabanClientMock, minioServiceSpy, 0));

    BenchFlowExperimentManagerApplication.setFabanManagerService(fabanManagerServiceSpy);

    executorService = BenchFlowExperimentManagerApplication.getExperimentTaskScheduler()
        .getExperimentTaskExecutorService();
  }

  @Test
  public void runValidExperiment() throws Exception {

    // make sure experiment has been saved to minio
    minioServiceSpy.saveExperimentDefinition(experimentID,
        MinioTestData.getExperiment1TrialDefinition());
    minioServiceSpy.saveExperimentDeploymentDescriptor(experimentID,
        MinioTestData.getDeploymentDescriptor());
    minioServiceSpy.saveExperimentBPMNModel(experimentID, MinioTestData.BPMN_MODEL_TEST_NAME,
        MinioTestData.getTestModel());
    minioServiceSpy.saveExperimentBPMNModel(experimentID, MinioTestData.BPMN_MODEL_MOCK_NAME,
        MinioTestData.getMockModel());

    // make sure also drivers-maker benchmark is returned
    Mockito.doReturn(MinioTestData.getGeneratedBenchmark()).when(minioServiceSpy)
        .getDriversMakerGeneratedBenchmark(Mockito.anyString(), Mockito.anyLong());

    // make sure also faban configuration file is returned
    Mockito.doReturn(MinioTestData.getFabanConfiguration()).when(minioServiceSpy)
        .getDriversMakerGeneratedFabanConfiguration(Mockito.anyString(), Mockito.anyLong(),
            Mockito.anyLong());

    Mockito.doReturn(new DeployStatus(201)).when(fabanClientMock).deploy(Mockito.any());

    RunId fabanRunId = new RunId("test.faban-id");
    String trialID = experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + 1;

    Mockito.doReturn(fabanRunId).when(fabanClientMock).submit(Mockito.anyString(),
        Mockito.anyString(), Mockito.any(File.class));

    // we mock this because otherwise waits 60s for first request to Faban
    Mockito.doAnswer(invocationOnMock -> {

      FabanStatusRequest fabanStatusRequest =
          new FabanStatusRequest(trialID, StatusCode.COMPLETED, Result.PASSED);

      sendFabanStatus(trialID, fabanStatusRequest);

      return null;

    }).when(fabanManagerServiceSpy).pollForTrialStatus(trialID, fabanRunId);

    // Drivers Maker Stub
    stubFor(post(urlEqualTo(DriversMakerService.GENERATE_BENCHMARK_PATH))
        .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode())));

    // Test Manager Trial Status Stub
    stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromTrialID(trialID)
        + BenchFlowTestManagerService.TRIAL_STATUS_PATH))
            .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())));

    // Test Manager Experiment State Stub
    stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromExperimentID(experimentID)
        + BenchFlowTestManagerService.EXPERIMENT_STATE_PATH))
            .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())));

    Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

    Response response = client.target(String.format("http://localhost:%d", RULE.getLocalPort()))
        .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
        .path(BenchFlowExperimentResource.RUN_ACTION_PATH).request().post(null);

    Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

    BenchFlowExperimentModelDAO experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient);

    Assert.assertNotNull(experimentModelDAO.getExperimentModel(experimentID));

    // TODO - alternative would be to have configuration setting to change first polling to 0s
    // wait long enough for tasks to start to be executed
    executorService.awaitTermination(5, TimeUnit.SECONDS);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));
    Assert.assertEquals(TerminatedState.COMPLETED,
        experimentModelDAO.getTerminatedState(experimentID));
  }

  @Test
  public void runInvalidExperiment() throws Exception {

    // experiment has not been saved to minio

    Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

    Response response = client.target(String.format("http://localhost:%d", RULE.getLocalPort()))
        .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
        .path(BenchFlowExperimentResource.RUN_ACTION_PATH).request().post(null);

    Assert.assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
  }

  private void sendFabanStatus(String trialID, FabanStatusRequest fabanStatusRequest) {
    // send status to experiment manager
    TrialResource trialResource = BenchFlowExperimentManagerApplication.getTrialResource();

    TrialIDElements trialIDElements = new TrialIDElements(trialID);

    trialResource.setFabanResult(trialIDElements.getUsername(), trialIDElements.getTestName(),
        trialIDElements.getTestNumber(), trialIDElements.getExperimentNumber(),
        trialIDElements.getTrialNumber(), fabanStatusRequest);
  }

}
