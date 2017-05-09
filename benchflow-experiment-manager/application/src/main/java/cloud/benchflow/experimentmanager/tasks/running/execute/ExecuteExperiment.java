package cloud.benchflow.experimentmanager.tasks.running.execute;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.ConfigFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.faban.client.responses.RunStatus.Code.QUEUED;
import static cloud.benchflow.faban.client.responses.RunStatus.Code.RECEIVED;
import static cloud.benchflow.faban.client.responses.RunStatus.Code.STARTED;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-07 */
public class ExecuteExperiment {

  public static RunStatus executeExperiment(
      String trialID,
      String experimentID,
      TrialModelDAO trialModelDAO,
      MinioService minioService,
      FabanClient fabanClient) {

    try {

      String fabanExperimentId =
          experimentID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);

      DriversMakerCompatibleID driversMakerCompatibleID =
          new DriversMakerCompatibleID(experimentID);

      String driversMakerExperimentID = driversMakerCompatibleID.getDriversMakerExperimentID();

      long experimentNumber = driversMakerCompatibleID.getExperimentNumber();

      // A) submit to fabanClient
      int submitRetries = BenchFlowExperimentManagerApplication.getSubmitRetries();

      int trialNumber = BenchFlowConstants.getTrialNumberFromTrialID(trialID);

      java.nio.file.Path fabanConfigPath =
          Paths.get(BenchFlowConstants.TEMP_DIR)
              .resolve(experimentID)
              .resolve(String.valueOf(trialNumber))
              .resolve(BenchFlowConstants.FABAN_CONFIGURATION_FILENAME);

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

      // TODO - when faban interaction changes and we don't have to do polling
      trialModelDAO.setFabanTrialID(experimentID, trialNumber, runId.toString());
      trialModelDAO.setTrialModelAsStarted(experimentID, trialNumber);

      // B) wait/poll for trial to complete and store the trial result in the DB
      // TODO - is this the status we want to use? No it is a subset, should also include metrics computation status
      RunStatus status = fabanClient.status(runId);

      while (status.getStatus().equals(QUEUED)
          || status.getStatus().equals(RECEIVED)
          || status.getStatus().equals(STARTED)) {
        Thread.sleep(1000);
        status = fabanClient.status(runId);
      }

      return status;

    } catch (IOException
        | InterruptedException
        | TrialIDDoesNotExistException
        | RunIdNotFoundException e) {

      // TODO - handle me properly
      e.printStackTrace();
      return new RunStatus(RunStatus.Code.FAILED.name(), null);
    }
  }
}
