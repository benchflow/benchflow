package cloud.benchflow.experimentmanager.services.external;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.faban.client.responses.RunStatus.Code.QUEUED;
import static cloud.benchflow.faban.client.responses.RunStatus.Code.RECEIVED;
import static cloud.benchflow.faban.client.responses.RunStatus.Code.STARTED;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial.TrialStatus;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.ConfigFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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

  public FabanManagerService(FabanClient fabanClient) {
    this.fabanClient = fabanClient;
  }

  public void initialize() {
    this.minioService = BenchFlowExperimentManagerApplication.getMinioService();
  }

  // used for testing
  public FabanManagerService(FabanClient fabanClient, MinioService minioService) {
    this.fabanClient = fabanClient;
    this.minioService = minioService;
  }

  // used for testing
  public FabanClient getFabanClient() {
    return fabanClient;
  }

  // used for testing
  public void setFabanClient(FabanClient fabanClient) {
    this.fabanClient = fabanClient;
  }

  public static String getFabanExperimentID(String experimentID) {
    return experimentID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);
  }

  public static String getFabanTrialID(String trialID) {
    return trialID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);
  }

  public void deployExperimentToFaban(String experimentID, String driversMakerExperimentID,
      long experimentNumber) throws IOException, JarFileNotFoundException {

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

    FileUtils.copyInputStreamToFile(fabanBenchmark, benchmarkPath.toFile());

    // deploy experiment to Faban
    fabanClient.deploy(benchmarkPath.toFile());

    // remove file that was sent to fabanClient
    FileUtils.forceDelete(benchmarkPath.toFile());

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
          // TODO - handle me
          throw e;
        }
      } catch (ConfigFileNotFoundException e) {
        // TODO - handle me
        e.printStackTrace();
      }
    }

    // remove file that was sent to fabanClient
    FileUtils.forceDelete(fabanConfigPath.toFile());

    return runId;

  }

  public TrialStatus pollForTrialStatus(String trialID, RunId runId) throws RunIdNotFoundException {

    // B) wait/poll for trial to complete and store the trial result in the DB
    // TODO - is this the status we want to use? No it is a subset, should also
    // include metrics computation status
    RunStatus status = fabanClient.status(runId);

    // workaround for issue #409 (FabanClient throws an exception when status is UNKNOWN)
    // wait 60s before polling (Faban needs time to setup)
    try {
      Thread.sleep(60 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    while (status.getStatus().equals(QUEUED) || status.getStatus().equals(RECEIVED)
        || status.getStatus().equals(STARTED)) {

      try {
        Thread.sleep(30 * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      status = fabanClient.status(runId);

    }

    return new TrialStatus(trialID, status.getStatus());

  }

}
