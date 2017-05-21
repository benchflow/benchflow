package cloud.benchflow.experimentmanager.tasks.running.execute;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.external.FabanManagerService;
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

import static cloud.benchflow.experimentmanager.services.external.FabanManagerService.getFabanTrialID;
import static cloud.benchflow.faban.client.responses.RunStatus.Code.*;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-07
 */
public class ExecuteTrial {

  public static TrialStatus executeTrial(String trialID, TrialModelDAO trialModelDAO,
      FabanManagerService fabanManagerService, FabanClient fabanClient) {

    try {

      String experimentID = BenchFlowConstants.getExperimentIDFromTrialID(trialID);

      DriversMakerCompatibleID driversMakerCompatibleID =
          new DriversMakerCompatibleID(experimentID);

      String driversMakerExperimentID = driversMakerCompatibleID.getDriversMakerExperimentID();

      long experimentNumber = driversMakerCompatibleID.getExperimentNumber();

      RunId runId = fabanManagerService.submitTrialToFaban(experimentID, trialID,
          driversMakerExperimentID, experimentNumber);

      trialModelDAO.setFabanTrialID(trialID, runId.toString());
      trialModelDAO.setTrialModelAsStarted(trialID);

      // B) wait/poll for trial to complete and store the trial result in the DB
      // TODO - is this the status we want to use? No it is a subset, should also include metrics computation status
      RunStatus status = fabanClient.status(runId);

      while (status.getStatus().equals(QUEUED) || status.getStatus().equals(RECEIVED)
          || status.getStatus().equals(STARTED)) {
        Thread.sleep(1000);
        status = fabanClient.status(runId);
      }

      return new TrialStatus(trialID, status.getStatus());

    } catch (IOException | InterruptedException | TrialIDDoesNotExistException
        | RunIdNotFoundException e) {

      // TODO - handle me properly
      e.printStackTrace();

      RunStatus status = new RunStatus(RunStatus.Code.FAILED.name(), null);
      return new TrialStatus(trialID, status.getStatus());
    }
  }

  public static class TrialStatus {
    private String trialID;
    private RunStatus.Code statusCode;

    public TrialStatus(String trialID, RunStatus.Code statusCode) {
      this.trialID = trialID;
      this.statusCode = statusCode;
    }

    public String getTrialID() {
      return trialID;
    }

    public RunStatus.Code getStatusCode() {
      return statusCode;
    }
  }
}
