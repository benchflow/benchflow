package cloud.benchflow.experimentmanager.helpers;

import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;

/**
 * Waits for test termination while triggering a callback.
 *
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public abstract class WaitExperimentTermination {

  public static void waitForExperimentTerminationWithTimeout(String experimentID,
      BenchFlowExperimentModelDAO experimentModelDAO, WaitExperimentCheck waitExperimentCheck,
      long timeout) throws BenchFlowExperimentIDDoesNotExistException, InterruptedException {

    long startTime = System.currentTimeMillis(); //fetch starting time

    while (!experimentModelDAO.getExperimentState(experimentID)
        .equals(BenchFlowExperimentModel.BenchFlowExperimentState.TERMINATED)
        && (System.currentTimeMillis() - startTime) < timeout) {

      waitExperimentCheck.checkExperimentIsFinished();

    }
  }
}
