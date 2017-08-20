package cloud.benchflow.experimentmanager.tasks.running.execute;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.faban.client.responses.RunId;
import java.io.IOException;

/**
 * @author Vincenzo Ferme (info@vincenzoferme.it) on 15.08.17
 */
public class ExecuteTrial {

  public static void executeTrial(String trialID, TrialModelDAO trialModelDAO,
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

      fabanManagerService.pollForTrialStatus(trialID, runId);

    } catch (IOException | TrialIDDoesNotExistException e) {

      // TODO - handle me properly
      e.printStackTrace();

    }

  }

}
