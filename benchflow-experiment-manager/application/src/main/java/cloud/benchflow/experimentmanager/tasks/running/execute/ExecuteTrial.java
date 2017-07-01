package cloud.benchflow.experimentmanager.tasks.running.execute;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.external.FabanManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunInfo;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus;
import java.io.IOException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-07
 */
public class ExecuteTrial {

  public static FabanStatus executeTrial(String trialID, TrialModelDAO trialModelDAO,
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

      return new FabanStatus(trialID, RunStatus.StatusCode.FAILED, Result.UNKNOWN);
    }

  }

  public static class FabanStatus {

    private String trialID;
    private RunStatus.StatusCode statusCode;
    private RunInfo.Result result;

    public FabanStatus(String trialID, RunStatus.StatusCode statusCode, RunInfo.Result result) {
      this.trialID = trialID;
      this.statusCode = statusCode;
      this.result = result;
    }

    public String getTrialID() {
      return trialID;
    }

    public RunStatus.StatusCode getStatusCode() {
      return statusCode;
    }

    public Result getResult() {
      return result;
    }
  }
}
