package cloud.benchflow.experimentmanager.tasks.running;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowExperiment;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.tasks.CancellableTask;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.ConfigFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.faban.client.responses.RunStatus.Code.*;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class ExperimentRunningTask extends CancellableTask {

  private static final String TEMP_DIR = "./tmp";
  private static final String FABAN_CONFIGURATION_FILENAME = "run.xml";
  private static Logger logger =
      LoggerFactory.getLogger(ExperimentRunningTask.class.getSimpleName());

  private String experimentID;
  private int submitRetries = 3;

  private BenchFlowTestManagerService testManagerService;
  private MinioService minioService;
  private FabanClient fabanClient;

  private BenchFlowExperimentModelDAO experimentModelDAO;

  public ExperimentRunningTask(String experimentID, int submitRetries) {

    this.experimentID = experimentID;
    this.submitRetries = submitRetries;

    this.testManagerService = BenchFlowExperimentManagerApplication.getTestManagerService();
    this.minioService = BenchFlowExperimentManagerApplication.getMinioService();
    this.fabanClient = BenchFlowExperimentManagerApplication.getFabanClient();
    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
  }

  @Override
  public void run() {

    logger.info("running experiment: " + experimentID);

    try {

      // get the BenchFlowExperimentDefinition from minioService
      String experimentYamlString =
          IOUtils.toString(
              minioService.getExperimentDefinition(experimentID), StandardCharsets.UTF_8);

      BenchFlowExperiment experiment =
          BenchFlowDSL.experimentFromExperimentYaml(experimentYamlString);

      int nTrials = experiment.configuration().terminationCriteria().get().experiment().number();

      String fabanExperimentId =
          experimentID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);

      // convert to old version and save to minio, and also a new experimentID to send to DriversMaker
      // generate DriversMaker compatible files on minio
      DriversMakerCompatibleID driversMakerCompatibleID =
          new DriversMakerCompatibleID(experimentID);
      String driversMakerExperimentID = driversMakerCompatibleID.getDriversMakerExperimentID();
      long experimentNumber = driversMakerCompatibleID.getExperimentNumber();

      for (int trialNumber = 1; trialNumber <= nTrials; trialNumber++) {

        if (isCanceled.booleanValue()) {
          break;
        }

        // set the state = RUNNING and status = EXECUTE_NEW_TRIAL for the experiment
        experimentModelDAO.setRunningState(experimentID, RunningState.EXECUTE_NEW_TRIAL);
        testManagerService.setExperimentRunningState(experimentID, RunningState.EXECUTE_NEW_TRIAL);

        // add trial to experiment
        String trialID = experimentModelDAO.addTrial(experimentID, trialNumber);

        // A) submit to fabanClient
        int retries = submitRetries;

        java.nio.file.Path fabanConfigPath =
            Paths.get(TEMP_DIR)
                .resolve(experimentID)
                .resolve(String.valueOf(trialNumber))
                .resolve(FABAN_CONFIGURATION_FILENAME);

        InputStream configInputStream =
            minioService.getDriversMakerGeneratedFabanConfiguration(
                driversMakerExperimentID, experimentNumber, trialNumber);
        String config = IOUtils.toString(configInputStream, StandardCharsets.UTF_8);

        FileUtils.writeStringToFile(fabanConfigPath.toFile(), config, StandardCharsets.UTF_8);

        RunId runId = null;
        while (runId == null) {
          try {

            // TODO - should this be a method (part of Faban Client?)
            String fabanTrialId =
                trialID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);

            runId = fabanClient.submit(fabanExperimentId, fabanTrialId, fabanConfigPath.toFile());

            // TODO - handle RESULT

            // TODO - handle FATAL FAILURE (SUT, load, execution)

          } catch (FabanClientException e) {

            if (retries > 0) {

              // if there was a FAILURE and we have not finished all retries
              experimentModelDAO.setRunningState(experimentID, RunningState.RE_EXECUTE_TRIAL);
              testManagerService.setExperimentRunningState(
                  experimentID, RunningState.RE_EXECUTE_TRIAL);

              retries--;

            } else {
              throw e;
            }
          } catch (ConfigFileNotFoundException e) {
            e.printStackTrace();
          }
        }

        experimentModelDAO.setFabanTrialID(experimentID, trialNumber, runId.toString());
        experimentModelDAO.setTrialModelAsStarted(experimentID, trialNumber);

        // B) wait/poll for trial to complete and store the trial result in the DB
        RunStatus status =
            fabanClient.status(
                runId); // TODO - is this the status we want to use? No it is a subset, should also include metrics computation status

        while (status.getStatus().equals(QUEUED)
            || status.getStatus().equals(RECEIVED)
            || status.getStatus().equals(STARTED)) {
          Thread.sleep(1000);
          status = fabanClient.status(runId);
        }

        experimentModelDAO.setTrialStatus(experimentID, trialNumber, status.getStatus());
        testManagerService.submitTrialStatus(trialID, status.getStatus());

        // TODO - check criteria and decide next steps

      }

      if (isCanceled.booleanValue()) {

        experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
        experimentModelDAO.setTerminatedState(experimentID, TerminatedState.ABORTED);
        testManagerService.setExperimentTerminatedState(experimentID, TerminatedState.ABORTED);

      } else {

        experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
        experimentModelDAO.setTerminatedState(experimentID, TerminatedState.COMPLETED);
        testManagerService.setExperimentTerminatedState(experimentID, TerminatedState.COMPLETED);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (TrialIDDoesNotExistException e) {
      e.printStackTrace();
    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    } catch (RunIdNotFoundException e) {
      e.printStackTrace();
    } catch (BenchFlowDeserializationException e) {
      // should not happen at this stage
      // TODO - handle me
      e.printStackTrace();
    }
  }
}
