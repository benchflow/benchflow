package cloud.benchflow.experimentmanager.tasks.running.execute;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.external.FabanManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import java.io.IOException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-07
 */
public class ExecuteTrial {

  public static TrialStatus executeTrial(String trialID, TrialModelDAO trialModelDAO,
      FabanManagerService fabanManagerService) {

    try {

      String experimentID = BenchFlowConstants.getExperimentIDFromTrialID(trialID);

      DriversMakerCompatibleID driversMakerCompatibleID =
          new DriversMakerCompatibleID(experimentID);

      String driversMakerExperimentID = driversMakerCompatibleID.getDriversMakerExperimentID();

      long experimentNumber = driversMakerCompatibleID.getExperimentNumber();

      RunId runId = fabanManagerService.submitTrialToFaban(experimentID, trialID,
          driversMakerExperimentID, experimentNumber);

      // TODO - this should be set by scheduler
      trialModelDAO.setFabanTrialID(trialID, runId.toString());
      trialModelDAO.setTrialModelAsStarted(trialID);

      // TODO - this should be a call from faban-manager
      return fabanManagerService.pollForTrialStatus(trialID, runId);

    } catch (IOException | TrialIDDoesNotExistException | RunIdNotFoundException e) {

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
