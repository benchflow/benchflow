package cloud.benchflow.testmanager.tasks;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.CompleteSelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;
import cloud.benchflow.testmanager.tasks.running.DetermineExecuteExperimentsTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20 */
public class BenchFlowTestTaskController {

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowTestTaskController.class.getSimpleName());

  private ConcurrentMap<String, Runnable> testTasks = new ConcurrentHashMap<>();

  private ExecutorService taskExecutorService;
  private BenchFlowTestModelDAO testModelDAO;
  private ExplorationModelDAO explorationModelDAO;

  public BenchFlowTestTaskController(ExecutorService taskExecutorService) {
    this.taskExecutorService = taskExecutorService;
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  // used for testing
  protected ExecutorService getTaskExecutorService() {
    return taskExecutorService;
  }

  /** @param testID */
  public synchronized void startComplete(String testID) {

    logger.info("startComplete with testID: " + testID);

    if (testTasks.containsKey(testID)) {
      logger.info("test already started");
      return;
    }

    try {

      // change state to ready
      testModelDAO.setTestState(testID, BenchFlowTestModel.BenchFlowTestState.READY);

      // move to next state
      runDetermineExecuteExperimentsTask(testID);

    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since already checked before
      logger.error("test could not be found");
    }
  }

  private synchronized void runDetermineExecuteExperimentsTask(String testID) {

    logger.info("runDetermineExecuteExperimentsTask with testID: " + testID);

    DetermineExecuteExperimentsTask task = new DetermineExecuteExperimentsTask(testID);

    // replace with new task
    testTasks.put(testID, task);

    // set test as running
    try {
      testModelDAO.setTestState(testID, BenchFlowTestModel.BenchFlowTestState.RUNNING);
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since already checked before
      logger.error("test could not be found");
      return;
    }

    // TODO - should go into a stateless queue (so that we can recover)
    taskExecutorService.submit(task);
  }

  public synchronized void handleExperimentResult(String experimentID) {

    logger.info("handleExperimentResult for experimentID: " + experimentID);

    String testID = BenchFlowConstants.getTestIDFromExperimentID(experimentID);

    if (!testTasks.containsKey(testID)) {
      logger.info("test not started");
      return;
    }

    try {

      ExperimentSelectionStrategy.Type selectionType =
          explorationModelDAO.getExperimentSelectionStrategyType(testID);

      switch (selectionType) {
        case COMPLETE_SELECTION:
          handleCompleteSelectionStrategyResult(testID);

          break;

          // TODO - other cases: should be delegated to a task that interacts with other services
        default:
          break;
      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private void handleCompleteSelectionStrategyResult(String testID)
      throws BenchFlowTestIDDoesNotExistException {
    CompleteSelectionStrategy completeSelectionStrategy =
        (CompleteSelectionStrategy) explorationModelDAO.getExperimentSelectionStrategy(testID);

    boolean testComplete = completeSelectionStrategy.isTestComplete(testID);

    if (testComplete) {

      testModelDAO.setTestState(testID, BenchFlowTestModel.BenchFlowTestState.TERMINATED);

      testTasks.remove(testID);

    } else {
      runDetermineExecuteExperimentsTask(testID);
    }
  }

  public synchronized void testMaxTimeReached(String testID) {

    logger.info("testMaxTimeReached: " + testID);

    // TODO - implement me

  }
}
