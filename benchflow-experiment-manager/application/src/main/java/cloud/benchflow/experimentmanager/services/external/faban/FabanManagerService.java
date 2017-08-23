package cloud.benchflow.experimentmanager.services.external.faban;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.api.request.FabanStatusRequest;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants.TrialIDElements;
import cloud.benchflow.experimentmanager.exceptions.BenchMarkDeploymentException;
import cloud.benchflow.experimentmanager.resources.TrialResource;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.BenchmarkNameNotFoundRuntimeException;
import cloud.benchflow.faban.client.exceptions.ConfigFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.DeployException;
import cloud.benchflow.faban.client.exceptions.EmptyHarnessResponseException;
import cloud.benchflow.faban.client.exceptions.FabanClientBadRequestException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.FabanClientIOException;
import cloud.benchflow.faban.client.exceptions.IllegalRunIdException;
import cloud.benchflow.faban.client.exceptions.IllegalRunInfoResultException;
import cloud.benchflow.faban.client.exceptions.IllegalRunStatusException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.DeployStatus;
import cloud.benchflow.faban.client.responses.DeployStatus.Code;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunInfo;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-21
 */
public class FabanManagerService {

  private static final String TEMP_DIR = "./tmp";
  private static final String BENCHMARK_FILE_ENDING = ".jar";

  private static Logger logger = LoggerFactory.getLogger(FabanManagerService.class.getSimpleName());

  private FabanClient fabanClient;
  private MinioService minioService;

  private int numConnectionRetries;

  public FabanManagerService(FabanClient fabanClient, int numConnectionRetries) {
    this.fabanClient = fabanClient;
    this.numConnectionRetries = numConnectionRetries;
  }

  @VisibleForTesting
  public FabanManagerService(FabanClient fabanClient, MinioService minioService,
      int numConnectionRetries) {
    this.fabanClient = fabanClient;
    this.minioService = minioService;
    this.numConnectionRetries = numConnectionRetries;
  }

  public static String getFabanExperimentID(String experimentID) {
    return experimentID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);
  }

  public static String getFabanTrialID(String trialID) {
    return trialID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);
  }

  public void initialize() {
    this.minioService = BenchFlowExperimentManagerApplication.getMinioService();
  }

  // used for testing
  public FabanClient getFabanClient() {
    return fabanClient;
  }

  // used for testing
  public void setFabanClient(FabanClient fabanClient) {
    this.fabanClient = fabanClient;
  }

  public void deployExperimentToFaban(String experimentID, String driversMakerExperimentID,
      long experimentNumber) throws BenchMarkDeploymentException {

    logger.info("deploying benchmark to Faban: " + experimentID);

    String fabanExperimentId = getFabanExperimentID(experimentID);

    // DEPLOY TO FABAN
    // get the generated benchflow-benchmark.jar from minioService and save to disk
    // so that it can be sent
    InputStream fabanBenchmark =
        minioService.getDriversMakerGeneratedBenchmark(driversMakerExperimentID, experimentNumber);

    // store on disk because there are issues sending InputStream directly
    java.nio.file.Path benchmarkPath = Paths.get(TEMP_DIR).resolve(experimentID)
        .resolve(fabanExperimentId + BENCHMARK_FILE_ENDING);

    try {

      FileUtils.copyInputStreamToFile(fabanBenchmark, benchmarkPath.toFile());

      // add retry policy for resilience
      RetryPolicy retryPolicy = new RetryPolicy().retryOn(FabanClientIOException.class)
          .abortOn(DeployException.class).abortOn(MalformedURIException.class)
          .withDelay(1, TimeUnit.SECONDS).withMaxRetries(numConnectionRetries);

      // deploy experiment to Faban
      DeployStatus deployStatus =
          Failsafe.with(retryPolicy).get(() -> fabanClient.deploy(benchmarkPath.toFile()));

      // if the benchmark could not be deployed
      if (deployStatus.getCode() != Code.CREATED) {
        throw new BenchMarkDeploymentException(experimentID,
            "DeployStatus is " + deployStatus.getCode());
      }

    } catch (Exception e) {
      throw new BenchMarkDeploymentException(experimentID, e.getMessage());
    } finally {

      if (benchmarkPath.toFile().exists()) {
        // remove file that was sent to fabanClient
        try {
          FileUtils.forceDelete(benchmarkPath.toFile());
        } catch (IOException e) {
          // we already check that the file exists so should not happen
          e.printStackTrace();
        }
      }


    }


  }

  public RunId submitTrialToFaban(String experimentID, String trialID,
      String driversMakerExperimentID, long experimentNumber) throws IOException {

    int submitRetries = BenchFlowExperimentManagerApplication.getSubmitRetries();
    int trialNumber = BenchFlowConstants.getTrialNumberFromTrialID(trialID);
    String fabanExperimentId = getFabanExperimentID(experimentID);

    java.nio.file.Path fabanConfigPath = Paths.get(BenchFlowConstants.TEMP_DIR)
        .resolve(experimentID).resolve(String.valueOf(trialNumber))
        .resolve(BenchFlowConstants.FABAN_CONFIGURATION_FILENAME);

    InputStream configInputStream = minioService.getDriversMakerGeneratedFabanConfiguration(
        driversMakerExperimentID, experimentNumber, trialNumber);

    String config = IOUtils.toString(configInputStream, StandardCharsets.UTF_8);

    FileUtils.writeStringToFile(fabanConfigPath.toFile(), config, StandardCharsets.UTF_8);

    RunId runId = null;
    while (runId == null) {

      try {

        String fabanTrialId = getFabanTrialID(trialID);

        runId = fabanClient.submit(fabanExperimentId, fabanTrialId, fabanConfigPath.toFile());

      } catch (FabanClientException e) {

        if (submitRetries > 0) {

          // if there was an error submitting and we have not finished all retries
          submitRetries--;

        } else {
          try {
            throw e;
          } catch (FabanClientIOException | ConfigFileNotFoundException
              | EmptyHarnessResponseException | IllegalRunIdException
              | BenchmarkNameNotFoundRuntimeException | MalformedURIException e1) {
            // TODO - handle me
            e1.printStackTrace();
          }
        }
      }
    }

    // remove file that was sent to fabanClient
    FileUtils.forceDelete(fabanConfigPath.toFile());

    return runId;

  }

  public void pollForTrialStatus(String trialID, RunId runId) {

    /*
      This code is executed as part of the DETERMINE_EXECUTE_TRIALS, and since it is in a thread
      it will be asynchronous, which will make the state of the experiment change to
      HANDLE_TRIAL_RESULT and the while loop will finish. Once the FabanManager has a result it
      will then invoke the Scheduler again and then the life cycle continues.
     */

    RetryPolicy pollRetryPolicy = new RetryPolicy().retryOn(FabanClientIOException.class) // retry if we get a timeout or similar
        .abortOn(IllegalRunStatusException.class).abortOn(MalformedURIException.class)
        .abortOn(FabanClientBadRequestException.class).abortOn(IllegalRunInfoResultException.class)
        .abortOn(RunIdNotFoundException.class).withDelay(10, TimeUnit.SECONDS)
        .withMaxRetries(numConnectionRetries);


    // execute in a thread to be asynchronous (similar to when faban manager is a service)
    new Thread(() -> {

      // B) wait/poll for trial to complete and store the trial result in the DB
      // TODO - is this the status we want to use? No it is a subset, should also
      // include metrics computation status
      RunStatus status;
      FabanStatusRequest fabanStatusRequest = null;

      try {

        // wait 60s before polling (Faban needs time to setup)
        try {
          Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        status = Failsafe.with(pollRetryPolicy).get(() -> fabanClient.status(runId));

        while (status.getStatus().equals(StatusCode.QUEUED)
            || status.getStatus().equals(StatusCode.RECEIVED)
            || status.getStatus().equals(StatusCode.STARTED)
            || status.getStatus().equals(StatusCode.UNKNOWN)) {

          try {
            Thread.sleep(30 * 1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          status = Failsafe.with(pollRetryPolicy).get(() -> fabanClient.status(runId));

        }

        RunInfo runInfo = Failsafe.with(pollRetryPolicy).get(() -> fabanClient.runInfo(runId));

        fabanStatusRequest =
            new FabanStatusRequest(trialID, status.getStatus(), runInfo.getResult());

      } catch (Exception e) {
        // TODO - handle me
        e.printStackTrace();

      } finally {

        // in case there was some error
        if (fabanStatusRequest == null) {
          //See https://github.com/benchflow/benchflow/pull/473/files#r128371872
          fabanStatusRequest = new FabanStatusRequest(trialID, StatusCode.UNKNOWN, Result.UNKNOWN);
        }

        // send status to experiment manager
        TrialResource trialResource = BenchFlowExperimentManagerApplication.getTrialResource();

        TrialIDElements trialIDElements = new TrialIDElements(trialID);

        // invoke the resource by calling the method
        trialResource.setFabanResult(trialIDElements.getUsername(), trialIDElements.getTestName(),
            trialIDElements.getTestNumber(), trialIDElements.getExperimentNumber(),
            trialIDElements.getTrialNumber(), fabanStatusRequest);
      }

    }).start();


  }

}
