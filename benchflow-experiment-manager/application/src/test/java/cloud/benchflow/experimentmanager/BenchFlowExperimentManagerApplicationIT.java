package cloud.benchflow.experimentmanager;

import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.helpers.MinioTestData;
import cloud.benchflow.experimentmanager.helpers.TestConstants;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.responses.DeployStatus;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-13 */
public class BenchFlowExperimentManagerApplicationIT extends DockerComposeIT {

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
          ConfigOverride.config("faban.address", TEST_ADDRESS));

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(TEST_PORT);

  private String experimentID = TestConstants.BENCHFLOW_EXPERIMENT_ID;
  private MinioService minioServiceSpy;
  private FabanClient fabanClientSpy;
  private ExecutorService executorService;

  @Before
  public void setUp() throws Exception {
    minioServiceSpy = Mockito.spy(BenchFlowExperimentManagerApplication.getMinioService());
    BenchFlowExperimentManagerApplication.setMinioService(minioServiceSpy);

    fabanClientSpy = Mockito.spy(BenchFlowExperimentManagerApplication.getFabanClient());
    BenchFlowExperimentManagerApplication.setFabanClient(fabanClientSpy);

    executorService = BenchFlowExperimentManagerApplication.getExperimentTaskController()
        .getExperimentTaskExecutorService();
  }

  @Test
  public void runValidExperiment() throws Exception {

    // make sure experiment has been saved to minio
    minioServiceSpy.saveExperimentDefinition(experimentID, MinioTestData.getExperimentDefinition());
    minioServiceSpy.saveExperimentDeploymentDescriptor(experimentID,
        MinioTestData.getDeploymentDescriptor());
    minioServiceSpy.saveExperimentBPMNModel(experimentID, MinioTestData.BPM_MODEL_11_PARALLEL_NAME,
        MinioTestData.get11ParallelStructuredModel());

    // make sure also drivers-maker benchmark is returned
    Mockito.doReturn(MinioTestData.getGeneratedBenchmark()).when(minioServiceSpy)
        .getDriversMakerGeneratedBenchmark(Mockito.anyString(), Mockito.anyLong());

    // make sure also faban configuration file is returned
    Mockito.doReturn(MinioTestData.getFabanConfiguration()).when(minioServiceSpy)
        .getDriversMakerGeneratedFabanConfiguration(Mockito.anyString(), Mockito.anyLong(),
            Mockito.anyLong());

    Mockito.doReturn(new DeployStatus(201)).when(fabanClientSpy).deploy(Mockito.any());

    RunId fabanRunId = new RunId("test.faban-id");

    Mockito.doReturn(fabanRunId).when(fabanClientSpy).submit(Mockito.anyString(),
        Mockito.anyString(), Mockito.any(File.class));

    Mockito.doReturn(new RunStatus("COMPLETED", fabanRunId)).when(fabanClientSpy)
        .status(fabanRunId);

    String trialID = experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + 1;

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

    // wait long enough for tasks to start to be executed
    executorService.awaitTermination(2, TimeUnit.SECONDS);

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
}
