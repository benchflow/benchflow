package cloud.benchflow.testmanager.helpers;

import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;

/**
 * Waits for test termination while triggering a callback.
 *
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public abstract class WaitTestTermination {

  public static void waitForTestTerminationWithTimeout(String testID,
      BenchFlowTestModelDAO testModelDAO, WaitTestCheck waitTestCheck, long timeout)
      throws BenchFlowTestIDDoesNotExistException, InterruptedException {

    long startTime = System.currentTimeMillis(); //fetch starting time

    while (!testModelDAO.getTestState(testID)
        .equals(BenchFlowTestModel.BenchFlowTestState.TERMINATED)
        && (System.currentTimeMillis() - startTime) < timeout) {

      waitTestCheck.checkTestIsFinished();

    }
  }
}
