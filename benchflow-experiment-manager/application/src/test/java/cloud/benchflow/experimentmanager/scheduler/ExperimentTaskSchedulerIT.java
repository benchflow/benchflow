package cloud.benchflow.experimentmanager.scheduler;

import static cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService.getFabanTrialID;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.DockerComposeIT;
import cloud.benchflow.experimentmanager.api.request.FabanStatusRequest;
import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants.TrialIDElements;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.helpers.WaitExperimentCheck;
import cloud.benchflow.experimentmanager.helpers.WaitExperimentTermination;
import cloud.benchflow.experimentmanager.helpers.data.BenchFlowData;
import cloud.benchflow.experimentmanager.helpers.data.MinioTestData;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.resources.TrialResource;
import cloud.benchflow.experimentmanager.scheduler.running.RunningStatesHandler;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.BenchmarkNameNotFoundRuntimeException;
import cloud.benchflow.faban.client.exceptions.ConfigFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.EmptyHarnessResponseException;
import cloud.benchflow.faban.client.exceptions.FabanClientIOException;
import cloud.benchflow.faban.client.exceptions.IllegalRunIdException;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.DeployStatus;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
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
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class ExperimentTaskSchedulerIT extends DockerComposeIT {

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

  private FabanClient fabanClientMock = Mockito.mock(FabanClient.class);

  private ExperimentTaskScheduler experimentTaskSchedulerSpy;
  private RunningStatesHandler runningStatesHandlerSpy;
  private ExecutorService experimentTaskExecutorServer;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private FabanManagerService fabanManagerServiceSpy;

  @Before
  public void setUp() throws Exception {

    experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();

    // spy on minio to return files saved by other services
    MinioService minioServiceSpy =
        Mockito.spy(BenchFlowExperimentManagerApplication.getMinioService());
    BenchFlowExperimentManagerApplication.setMinioService(minioServiceSpy);

    // set faban client as mock
    fabanManagerServiceSpy =
        Mockito.spy(new FabanManagerService(fabanClientMock, minioServiceSpy, 0));
    BenchFlowExperimentManagerApplication.setFabanManagerService(fabanManagerServiceSpy);

    Mockito.doAnswer(invocationOnMock -> MinioTestData.getExperiment1TrialDefinition())
        .when(minioServiceSpy)
        .getExperimentDefinition(Mockito.contains(BenchFlowData.VALID_TEST_ID_1_TRIAL));

    Mockito.doAnswer(invocationOnMock -> MinioTestData.getExperiment2TrialsDefinition())
        .when(minioServiceSpy)
        .getExperimentDefinition(Mockito.contains(BenchFlowData.VALID_TEST_ID_2_TRIAL));

    Mockito.doAnswer(invocationOnMock -> MinioTestData.getDeploymentDescriptor())
        .when(minioServiceSpy).getExperimentDeploymentDescriptor(Mockito.anyString());

    Mockito.doAnswer(invocationOnMock -> MinioTestData.getTestModel()).when(minioServiceSpy)
        .getExperimentBPMNModel(Mockito.anyString(),
            Mockito.matches(MinioTestData.BPMN_MODEL_TEST_NAME));

    Mockito.doAnswer(invocationOnMock -> MinioTestData.getGeneratedBenchmark())
        .when(minioServiceSpy)
        .getDriversMakerGeneratedBenchmark(Mockito.anyString(), Mockito.anyLong());

    Mockito.doAnswer(invocationOnMock -> MinioTestData.getFabanConfiguration())
        .when(minioServiceSpy).getDriversMakerGeneratedFabanConfiguration(Mockito.anyString(),
            Mockito.anyLong(), Mockito.anyLong());

    Mockito.doNothing().when(minioServiceSpy).copyDeploymentDescriptorForDriversMaker(
        Mockito.anyString(), Mockito.anyString(), Mockito.anyLong());

    Mockito.doNothing().when(minioServiceSpy).copyExperimentBPMNModelForDriversMaker(
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

    Mockito.doNothing().when(minioServiceSpy).copyExperimentDefintionForDriversMaker(
        Mockito.anyString(), Mockito.anyLong(), Mockito.any(InputStream.class));

    // Drivers Maker Stub
    stubFor(post(urlEqualTo(DriversMakerService.GENERATE_BENCHMARK_PATH))
        .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode())));

    Mockito.doReturn(new DeployStatus(201)).when(fabanClientMock).deploy(Mockito.any());

    experimentTaskSchedulerSpy =
        Mockito.spy(BenchFlowExperimentManagerApplication.getExperimentTaskScheduler());

    BenchFlowExperimentManagerApplication.setExperimentTaskScheduler(experimentTaskSchedulerSpy);

    BenchFlowExperimentManagerApplication.getTrialResource()
        .setExperimentTaskScheduler(experimentTaskSchedulerSpy);

    runningStatesHandlerSpy = Mockito.spy(experimentTaskSchedulerSpy.getRunningStatesHandler());

    experimentTaskSchedulerSpy.setRunningStatesHandler(runningStatesHandlerSpy);

    experimentTaskExecutorServer = experimentTaskSchedulerSpy.getExperimentTaskExecutorService();

  }

  @Test
  public void runSingleExperimentSingleTrial() throws Exception {

    String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_1_TRIAL;

    setupExperimentMocks(experimentID);
    setupTrialMocksSuccessful(experimentID, 1);

    experimentModelDAO.addExperiment(experimentID);

    experimentTaskSchedulerSpy.handleStartingExperiment(experimentID);

    waitExperimentTerminationForOneMinute(experimentID);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));

    Assert.assertEquals(TerminatedState.COMPLETED,
        experimentModelDAO.getTerminatedState(experimentID));
  }

  @Test
  public void runSingleExperimentSingleTrialWithRandomFailureAndSuccess() throws Exception {

    String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_1_TRIAL;

    setupExperimentMocks(experimentID);
    setupTrialMocksWithRandomFailureAndSuccess(experimentID, 1);

    experimentModelDAO.addExperiment(experimentID);

    experimentTaskSchedulerSpy.handleStartingExperiment(experimentID);

    waitExperimentTerminationForOneMinute(experimentID);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));

    Assert.assertEquals(TerminatedState.COMPLETED,
        experimentModelDAO.getTerminatedState(experimentID));

    // assert that the trial has been re-executed
    Mockito.verify(runningStatesHandlerSpy, Mockito.atLeast(1))
        .handleReExecuteTrial(Matchers.anyString());
  }

  @Test
  public void runSingleExperimentSingleTrialWithExperimentFailure() throws Exception {

    String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_1_TRIAL;

    setupExperimentMocks(experimentID);
    setupTrialMocksWithOnlyFailure(experimentID, 1);

    experimentModelDAO.addExperiment(experimentID);

    experimentTaskSchedulerSpy.handleStartingExperiment(experimentID);

    waitExperimentTerminationForOneMinute(experimentID);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));

    Assert.assertEquals(TerminatedState.FAILURE,
        experimentModelDAO.getTerminatedState(experimentID));
  }

  @Test
  public void runSingleExperimentMultipleTrials() throws Exception {

    String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_2_TRIAL;

    setupExperimentMocks(experimentID);

    for (int i = 1; i <= 2; i++) {
      setupTrialMocksSuccessful(experimentID, i);
    }

    experimentModelDAO.addExperiment(experimentID);

    experimentTaskSchedulerSpy.handleStartingExperiment(experimentID);

    waitExperimentTerminationForOneMinute(experimentID);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));

    Assert.assertEquals(TerminatedState.COMPLETED,
        experimentModelDAO.getTerminatedState(experimentID));

    Assert.assertEquals(2, experimentModelDAO.getNumExecutedTrials(experimentID));
  }

  @Test
  public void runSingleExperimentMultipleTrialsWithExperimentFailure() throws Exception {

    String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_2_TRIAL;

    setupExperimentMocks(experimentID);

    for (int i = 1; i <= 2; i++) {
      setupTrialMocksWithOnlyFailure(experimentID, i);
    }

    experimentModelDAO.addExperiment(experimentID);

    experimentTaskSchedulerSpy.handleStartingExperiment(experimentID);

    waitExperimentTerminationForOneMinute(experimentID);

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));

    Assert.assertEquals(TerminatedState.FAILURE,
        experimentModelDAO.getTerminatedState(experimentID));

    Assert.assertEquals(1, experimentModelDAO.getNumExecutedTrials(experimentID));

  }

  private void waitExperimentTerminationForOneMinute(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException, InterruptedException {
    long timeout = 1 * 60 * 1000; //1 minute

    // check when the experiment reaches the final state, with a timeout
    WaitExperimentCheck waitExperimentCheck = () -> {

      // wait for tasks to finish
      experimentTaskExecutorServer.awaitTermination(1, TimeUnit.SECONDS);

    };

    WaitExperimentTermination.waitForExperimentTerminationWithTimeout(experimentID,
        experimentModelDAO, waitExperimentCheck, timeout);
  }

  @Test
  public void runMultipleExperimentMultipleTrialsWithRandomFailures() throws Exception {

    int[] experimentNumbers = new int[] {1, 2};
    String[] experimentIDs = new String[2];

    for (int i = 0; i < experimentNumbers.length; i++) {

      String experimentID =
          BenchFlowData.getValidExperimentID2TrialFromNumber(experimentNumbers[i]);

      experimentIDs[i] = experimentID;

      setupExperimentMocks(experimentID);

      for (int j = 1; j <= 2; j++) {
        setupTrialMocksWithRandomFailureAndSuccess(experimentID, j);
      }

      experimentModelDAO.addExperiment(experimentID);

    }

    long timeout = 1 * 60 * 1000; //1 minute

    // check when the experiment reaches the final state, with a timeout
    WaitExperimentCheck waitExperimentCheck = () -> {

      // wait for tasks to finish
      experimentTaskExecutorServer.awaitTermination(1, TimeUnit.SECONDS);

    };

    // schedule experiments one after the other
    for (String experimentID : experimentIDs) {
      experimentTaskSchedulerSpy.handleStartingExperiment(experimentID);
      WaitExperimentTermination.waitForExperimentTerminationWithTimeout(experimentID,
          experimentModelDAO, waitExperimentCheck, timeout);
    }

    for (String experimentID : experimentIDs) {
      Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
          experimentModelDAO.getExperimentState(experimentID));

      Assert.assertEquals(TerminatedState.COMPLETED,
          experimentModelDAO.getTerminatedState(experimentID));

      Assert.assertEquals(2, experimentModelDAO.getNumExecutedTrials(experimentID));
    }

  }

  @Test
  public void abortRunningExperimentNoTrialOnFaban() throws Exception {

    String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_2_TRIAL;

    setupExperimentMocks(experimentID);

    for (int i = 1; i <= 2; i++) {
      setupTrialMocksSuccessful(experimentID, i);
    }

    // abort the test in CheckTerminationCriteria
    Mockito.doAnswer(invocationOnMock -> {

      // trigger the abort
      new Thread(() -> experimentTaskSchedulerSpy.abortExperiment(experimentID)).start();

      // call the regular implementation
      return invocationOnMock.callRealMethod();

    }).when(runningStatesHandlerSpy).handleCheckTerminationCriteria(experimentID);

    experimentModelDAO.addExperiment(experimentID);

    experimentTaskSchedulerSpy.handleStartingExperiment(experimentID);

    waitExperimentTerminationForOneMinute(experimentID);

    // assertions

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));

    Assert.assertEquals(TerminatedState.ABORTED,
        experimentModelDAO.getTerminatedState(experimentID));

    Mockito.verify(runningStatesHandlerSpy, Mockito.times(1)).handleTerminating(experimentID);

    Mockito.verify(experimentTaskSchedulerSpy, Mockito.times(1))
        .handleTerminatedState(experimentID);

  }

  @Test
  public void abortRunningExperimentWithTrialOnFaban() throws Exception {

    String experimentID = BenchFlowData.VALID_EXPERIMENT_ID_2_TRIAL;

    setupExperimentMocks(experimentID);

    for (int i = 1; i <= 2; i++) {
      setupTrialMocksSuccessful(experimentID, i);
    }

    // abort the test in HandleDetermineAndExecuteTrials to wait for Faban Manager
    Mockito.doAnswer(invocationOnMock -> {

      new Thread(() -> {

        // TODO - try to find a way to deterministically execute some code, after a
        // given mocked method is called.

        try {
          Thread.sleep(500);
          experimentTaskSchedulerSpy.abortExperiment(experimentID);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }).start();

      // call the regular implementation
      return invocationOnMock.callRealMethod();

    }).when(runningStatesHandlerSpy).handleDetermineAndExecuteTrials(experimentID);

    experimentModelDAO.addExperiment(experimentID);

    experimentTaskSchedulerSpy.handleStartingExperiment(experimentID);

    waitExperimentTerminationForOneMinute(experimentID);

    // assertions

    Assert.assertEquals(BenchFlowExperimentState.TERMINATED,
        experimentModelDAO.getExperimentState(experimentID));

    Assert.assertEquals(TerminatedState.ABORTED,
        experimentModelDAO.getTerminatedState(experimentID));

    Mockito.verify(runningStatesHandlerSpy, Mockito.times(1)).handleTerminating(experimentID);

    Mockito.verify(experimentTaskSchedulerSpy, Mockito.times(1))
        .handleTerminatedState(experimentID);

  }

  private void setupTrialMocksSuccessful(String experimentID, long trialNumber)
      throws JarFileNotFoundException, ConfigFileNotFoundException, RunIdNotFoundException,
      IllegalRunIdException, BenchmarkNameNotFoundRuntimeException, FabanClientIOException,
      MalformedURIException, EmptyHarnessResponseException {

    String fabanID = "test_faban_id_" + trialNumber;
    String fabanExperimentId = FabanManagerService.getFabanExperimentID(experimentID);

    RunId runId = new RunId(fabanID, Long.toString(trialNumber));

    String trialID = BenchFlowConstants.getTrialID(experimentID, trialNumber);

    // Test Manager Trial Status Stub
    stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromTrialID(trialID)
        + BenchFlowTestManagerService.TRIAL_STATUS_PATH))
            .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())));

    Mockito.doReturn(runId).when(fabanClientMock).submit(Mockito.matches(fabanExperimentId),
        Mockito.matches(getFabanTrialID(trialID)), Mockito.any(File.class));

    // TODO - alternative would be to have configuration setting to change first polling to 0s
    // we mock this because otherwise we have to wait for first polling (60s)
    Mockito.doAnswer(invocationOnMock -> {

      simulateFabanExecution(trialID,
          new FabanStatusRequest(trialID, StatusCode.COMPLETED, Result.PASSED));

      return null;

    }).when(fabanManagerServiceSpy).pollForTrialStatus(trialID, runId);

  }

  private void sendFabanStatus(String trialID, FabanStatusRequest fabanStatusRequest) {
    // send status to experiment manager
    TrialResource trialResource = BenchFlowExperimentManagerApplication.getTrialResource();

    TrialIDElements trialIDElements = new TrialIDElements(trialID);

    trialResource.setFabanResult(trialIDElements.getUsername(), trialIDElements.getTestName(),
        trialIDElements.getTestNumber(), trialIDElements.getExperimentNumber(),
        trialIDElements.getTrialNumber(), fabanStatusRequest);
  }

  private void setupTrialMocksWithRandomFailureAndSuccess(String experimentID, long trialNumber)
      throws RunIdNotFoundException, ConfigFileNotFoundException, IllegalRunIdException,
      BenchmarkNameNotFoundRuntimeException, FabanClientIOException, MalformedURIException,
      EmptyHarnessResponseException {

    String fabanID = "test_faban_id_" + trialNumber;
    String fabanExperimentId = FabanManagerService.getFabanExperimentID(experimentID);

    RunId runId = new RunId(fabanID, Long.toString(trialNumber));

    String trialID = BenchFlowConstants.getTrialID(experimentID, trialNumber);

    // Test Manager Trial Status Stub
    stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromTrialID(trialID)
        + BenchFlowTestManagerService.TRIAL_STATUS_PATH))
            .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())));

    Mockito.doReturn(runId).when(fabanClientMock).submit(Mockito.matches(fabanExperimentId),
        Mockito.matches(getFabanTrialID(trialID)), Mockito.any(File.class));

    // TODO - alternative would be to have configuration setting to change first polling to 0s
    // we mock this because otherwise we have to wait for first polling (60s)
    Mockito.doAnswer(invocationOnMock -> {

      simulateFabanExecution(trialID,
          new FabanStatusRequest(trialID, StatusCode.COMPLETED, Result.UNKNOWN));

      return null;

    }).doAnswer(invocationOnMock -> {

      simulateFabanExecution(trialID,
          new FabanStatusRequest(trialID, StatusCode.COMPLETED, Result.PASSED));

      return null;

    }).when(fabanManagerServiceSpy).pollForTrialStatus(trialID, runId);

  }

  private void simulateFabanExecution(String trialID, FabanStatusRequest fabanStatusRequest) {

    new Thread(() -> {

      try {

        // sleep to simulate execution
        Thread.sleep(2000);

        sendFabanStatus(trialID, fabanStatusRequest);

      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    }).start();

  }

  private void setupTrialMocksWithOnlyFailure(String experimentID, long trialNumber)
      throws RunIdNotFoundException, ConfigFileNotFoundException, IllegalRunIdException,
      BenchmarkNameNotFoundRuntimeException, FabanClientIOException, MalformedURIException,
      EmptyHarnessResponseException {

    String fabanID = "test_faban_id_" + trialNumber;
    String fabanExperimentId = FabanManagerService.getFabanExperimentID(experimentID);

    RunId runId = new RunId(fabanID, Long.toString(trialNumber));

    String trialID = BenchFlowConstants.getTrialID(experimentID, trialNumber);

    // Test Manager Trial Status Stub
    stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromTrialID(trialID)
        + BenchFlowTestManagerService.TRIAL_STATUS_PATH))
            .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())));

    Mockito.doReturn(runId).when(fabanClientMock).submit(Mockito.matches(fabanExperimentId),
        Mockito.matches(getFabanTrialID(trialID)), Mockito.any(File.class));

    // TODO - alternative would be to have configuration setting to change first polling to 0s
    // we mock this because otherwise we have to wait for first polling (60s)
    Mockito.doAnswer(invocationOnMock -> {

      FabanStatusRequest fabanStatusRequest =
          new FabanStatusRequest(trialID, StatusCode.FAILED, Result.FAILED);

      sendFabanStatus(trialID, fabanStatusRequest);

      return null;

    }).when(fabanManagerServiceSpy).pollForTrialStatus(trialID, runId);


  }

  private void setupExperimentMocks(String experimentID) {

    // Test Manager Experiment State Stub
    stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromExperimentID(experimentID)
        + BenchFlowTestManagerService.EXPERIMENT_STATE_PATH))
            .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())));


  }


}
