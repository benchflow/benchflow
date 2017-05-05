package cloud.benchflow.testmanager.tasks;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestTerminatedState;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.CompleteSelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;
import cloud.benchflow.testmanager.tasks.running.AddStoredKnowledgeTask;
import cloud.benchflow.testmanager.tasks.running.DerivePredictionFunctionTask;
import cloud.benchflow.testmanager.tasks.running.DetermineExecuteExperimentsTask;
import cloud.benchflow.testmanager.tasks.running.DetermineExplorationStrategyTask;
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

  public synchronized void handleTestState(String testID) {

    try {

      BenchFlowTestState testState = testModelDAO.getTestState(testID);

      switch (testState) {
        case START:

          // change state to ready
          testModelDAO.setTestState(testID, BenchFlowTestState.READY);

          handleTestState(testID);

          break;

        case READY:

          // change state to running
          testModelDAO.setTestState(testID, BenchFlowTestState.RUNNING);

          handleTestState(testID);

          break;

        case RUNNING:

          // handle running states
          handleTestRunningState(testID);

          break;
        case WAITING:

          // resume running

          break;

        case TERMINATED:

          // test already executed
          break;

        default:
          // no default
          break;
      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private synchronized void handleTestRunningState(String testID) {

    try {

      TestRunningState testRunningState = testModelDAO.getTestRunningState(testID);

      switch (testRunningState) {
        case DETERMINE_EXPLORATION_STRATEGY:
          testModelDAO.setTestRunningState(testID, TestRunningState.ADD_STORED_KNOWLEDGE);

          determineExplorationStrategy(testID);

          break;

        case ADD_STORED_KNOWLEDGE:
          testModelDAO.setTestRunningState(testID, TestRunningState.DETERMINE_EXECUTE_EXPERIMENTS);

          addStoredKnowledge(testID);

          break;

        case DETERMINE_EXECUTE_EXPERIMENTS:
          testModelDAO.setTestRunningState(testID, TestRunningState.HANDLE_EXPERIMENT_RESULT);

          determineAndExecuteExperiments(testID);

          break;

        case HANDLE_EXPERIMENT_RESULT:

          // this is handled separately in handleExperimentResult as
          // it is per experiment

          break;

        case DERIVE_PREDICTION_FUNCTION:
          testModelDAO.setTestRunningState(testID, TestRunningState.VALIDATE_PREDICTION_FUNCTION);

          derivePredictionFunction(testID);

          break;

        case VALIDATE_PREDICTION_FUNCTION:
          break;

        case REMOVE_NON_REACHABLE_EXPERIMENTS:
          break;

        default:
          break;
      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private void determineExplorationStrategy(String testID) {

    logger.info("determineExplorationStrategy with testID: " + testID);

    DetermineExplorationStrategyTask task = new DetermineExplorationStrategyTask(testID);

    // replace task
    testTasks.put(testID, task);

    taskExecutorService.submit(task);
  }

  private void addStoredKnowledge(String testID) {

    logger.info("addStoredKnowledge with testID: " + testID);

    AddStoredKnowledgeTask task = new AddStoredKnowledgeTask(testID);

    // replace task
    testTasks.put(testID, task);

    taskExecutorService.submit(task);
  }

  private void determineAndExecuteExperiments(String testID) {

    logger.info("determineAndExecuteExperiments with testID: " + testID);

    DetermineExecuteExperimentsTask task = new DetermineExecuteExperimentsTask(testID);

    // replace with new task
    testTasks.put(testID, task);

    // TODO - should go into a stateless queue (so that we can recover)
    taskExecutorService.submit(task);
  }

  private void derivePredictionFunction(String testID) {

    logger.info("derivePredictionFunction with testID: " + testID);

    DerivePredictionFunctionTask task = new DerivePredictionFunctionTask(testID);

    // replace with new task
    testTasks.put(testID, task);

    // TODO - should go into a stateless queue (so that we can recover)
    taskExecutorService.submit(task);
  }

  /**
   * Handles the result after experiment execution.
   *
   * @param experimentID ID of the experiment
   */
  public synchronized void handleExperimentResult(String experimentID) {

    logger.info("handleExperimentResult for experimentID: " + experimentID);

    String testID = BenchFlowConstants.getTestIDFromExperimentID(experimentID);

    try {

      BenchFlowTestState state = testModelDAO.getTestState(testID);

      if (state != BenchFlowTestState.RUNNING) {
        logger.info("test not running");
        return;
      }

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

      testModelDAO.setTestState(testID, BenchFlowTestState.TERMINATED);
      testModelDAO.setTestTerminatedState(testID, TestTerminatedState.GOAL_REACHED);

      testTasks.remove(testID);

    } else {

      // update the running state
      testModelDAO.setTestRunningState(testID, TestRunningState.DETERMINE_EXECUTE_EXPERIMENTS);

      // run the next state
      handleTestState(testID);
    }
  }

  public synchronized void testMaxTimeReached(String testID) {

    logger.info("testMaxTimeReached: " + testID);

    // TODO - implement me

  }
}
