package cloud.benchflow.experimentmanager.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.DockerComposeIT;
import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.helpers.MinioTestData;
import cloud.benchflow.experimentmanager.helpers.TestConstants;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.responses.DeployStatus;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class ExperimentTaskControllerIT extends DockerComposeIT {

  private static final int TEST_PORT = 8080;
  private static final String TEST_ADDRESS = "localhost:" + TEST_PORT;

  // TODO - this test is pretty much the same as the BenchFlowExperimentManagerApplictionIT so
  // TODO - we should consider how to remove redundancy

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

  private FabanClient fabanClientMock = Mockito.mock(FabanClient.class);

  private String experimentID = TestConstants.BENCHFLOW_EXPERIMENT_ID;

  private ExperimentTaskController experimentTaskController;
  private ExecutorService experimentTaskExecutorServer;
  private BenchFlowExperimentModelDAO experimentModelDAO;

  @Before
  public void setUp() throws Exception {

    DriversMakerCompatibleID driversMakerCompatibleID = new DriversMakerCompatibleID(experimentID);

    experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();

    // spy on minio to return files saved by other services
    MinioService minioServiceSpy =
        Mockito.spy(BenchFlowExperimentManagerApplication.getMinioService());
    BenchFlowExperimentManagerApplication.setMinioService(minioServiceSpy);

    // set faban client as mock
    BenchFlowExperimentManagerApplication.setFabanClient(fabanClientMock);

    Mockito.doAnswer(invocationOnMock -> MinioTestData.getExperimentDefinition())
        .when(minioServiceSpy).getExperimentDefinition(experimentID);
    Mockito.doReturn(MinioTestData.getDeploymentDescriptor()).when(minioServiceSpy)
        .getExperimentDeploymentDescriptor(experimentID);
    Mockito.doReturn(MinioTestData.get11ParallelStructuredModel()).when(minioServiceSpy)
        .getExperimentBPMNModel(experimentID, MinioTestData.BPM_MODEL_11_PARALLEL_NAME);
    Mockito.doReturn(MinioTestData.getGeneratedBenchmark()).when(minioServiceSpy)
        .getDriversMakerGeneratedBenchmark(driversMakerCompatibleID.getDriversMakerExperimentID(),
            driversMakerCompatibleID.getExperimentNumber());
    Mockito.doReturn(MinioTestData.getFabanConfiguration()).when(minioServiceSpy)
        .getDriversMakerGeneratedFabanConfiguration(
            driversMakerCompatibleID.getDriversMakerExperimentID(),
            driversMakerCompatibleID.getExperimentNumber(), 1);

    Mockito.doNothing().when(minioServiceSpy).copyDeploymentDescriptorForDriversMaker(experimentID,
        driversMakerCompatibleID.getDriversMakerExperimentID(),
        driversMakerCompatibleID.getExperimentNumber());
    Mockito.doNothing().when(minioServiceSpy).copyExperimentBPMNModelForDriversMaker(
        Mockito.matches(experimentID),
        Mockito.matches(driversMakerCompatibleID.getDriversMakerExperimentID()),
        Mockito.any(String.class));
    Mockito.doNothing().when(minioServiceSpy).copyExperimentDefintionForDriversMaker(
        Mockito.matches(driversMakerCompatibleID.getDriversMakerExperimentID()),
        Mockito.eq(driversMakerCompatibleID.getExperimentNumber()), Mockito.any(InputStream.class));

    experimentTaskController = BenchFlowExperimentManagerApplication.getExperimentTaskController();

    experimentTaskExecutorServer = experimentTaskController.getExperimentTaskExecutorService();
  }

  @Test
  public void runExperiment() throws Exception {

    String fabanID = "test_faban_id";
    RunId runId = new RunId(fabanID, "1");
    RunStatus status = new RunStatus("COMPLETED", runId);
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

    Mockito.doReturn(new DeployStatus(201)).when(fabanClientMock).deploy(Mockito.any());

    Mockito.doReturn(runId).when(fabanClientMock).submit(Mockito.anyString(), Mockito.anyString(),
        Mockito.any(File.class));

    Mockito.doReturn(new RunStatus("COMPLETED", runId)).when(fabanClientMock).status(runId);

    Mockito.doReturn(status).when(fabanClientMock).status(runId);

    experimentModelDAO.addExperiment(experimentID);

    experimentTaskController.handleExperimentState(experimentID);

    // wait for tasks to finish
    experimentTaskExecutorServer.awaitTermination(1, TimeUnit.SECONDS);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));

    Assert.assertEquals(TerminatedState.COMPLETED,
        experimentModelDAO.getTerminatedState(experimentID));
  }
}
