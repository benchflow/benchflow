package cloud.benchflow.testmanager.tasks;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.tasks.running.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestTerminatedState.GOAL_REACHED;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public class BenchFlowTestTaskController {

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowTestTaskController.class.getSimpleName());

  private ConcurrentMap<String, Future> testTasks = new ConcurrentHashMap<>();

  private ExecutorService taskExecutorService;
  private BenchFlowTestModelDAO testModelDAO;

  public BenchFlowTestTaskController(ExecutorService taskExecutorService) {
    this.taskExecutorService = taskExecutorService;
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
  }

  // used for testing
  protected ExecutorService getTaskExecutorService() {
    return taskExecutorService;
  }

  public synchronized void handleTestState(String testID) {

    try {

      BenchFlowTestState testState = testModelDAO.getTestState(testID);

      logger.info("handleTestState for " + testID + " with state " + testState.name());

      switch (testState) {
        case START:

          setNextTestState(testID, BenchFlowTestState.READY);

          break;

        case READY:

          setNextTestState(testID, BenchFlowTestState.RUNNING);

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
          logger.info("Test already executed. Nothing to do.");
          break;

        default:
          // no default
          break;
      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private void setNextTestState(String testID, BenchFlowTestState running) throws BenchFlowTestIDDoesNotExistException {
    // change state to running
    testModelDAO.setTestState(testID, running);

    handleTestState(testID);
  }

  private synchronized void handleTestRunningState(String testID) {

    try {

      TestRunningState testRunningState = testModelDAO.getTestRunningState(testID);

      logger.info(
          "handleTestRunningState for " + testID + " with state " + testRunningState.name());

      switch (testRunningState) {
        case DETERMINE_EXPLORATION_STRATEGY:
          determineExplorationStrategy(testID);

          break;

        case ADD_STORED_KNOWLEDGE:
          addStoredKnowledge(testID);

          break;

        case DETERMINE_EXECUTE_EXPERIMENTS:
          determineAndExecuteExperiments(testID);

          break;

        case HANDLE_EXPERIMENT_RESULT:
          handleExperimentResult(testID);

          break;

        case VALIDATE_TERMINATION_CRITERIA:
          validateTerminationCriteria(testID);

          break;

        case DERIVE_PREDICTION_FUNCTION:
          testModelDAO.setTestRunningState(testID, TestRunningState.VALIDATE_PREDICTION_FUNCTION);
          derivePredictionFunction(testID);

          break;

        case VALIDATE_PREDICTION_FUNCTION:
          validatePredictionFunction(testID);

          break;

        case REMOVE_NON_REACHABLE_EXPERIMENTS:
          removeNonReachableExperiments(testID);

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

    // TODO - should go into a stateless queue (so that we can recover)
    Future<?> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, TestRunningState.ADD_STORED_KNOWLEDGE);
  }

  private void addStoredKnowledge(String testID) {

    logger.info("addStoredKnowledge with testID: " + testID);

    AddStoredKnowledgeTask task = new AddStoredKnowledgeTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<?> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, TestRunningState.DETERMINE_EXECUTE_EXPERIMENTS);
  }

  private void determineAndExecuteExperiments(String testID) {

    logger.info("determineAndExecuteExperiments with testID: " + testID);

    DetermineExecuteExperimentsTask task = new DetermineExecuteExperimentsTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<?> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    // we don't wait for the task to complete since the experiment-manager
    // will send the result
    try {

      testModelDAO.setTestRunningState(testID, TestRunningState.HANDLE_EXPERIMENT_RESULT);

    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }
  }

  private void derivePredictionFunction(String testID) {

    logger.info("derivePredictionFunction with testID: " + testID);

    DerivePredictionFunctionTask task = new DerivePredictionFunctionTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<?> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, TestRunningState.VALIDATE_PREDICTION_FUNCTION);
  }

  private void validatePredictionFunction(String testID) {

    logger.info("validatePredictionFunction with testID: " + testID);

    ValidatePredictionFunctionTask task = new ValidatePredictionFunctionTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<Boolean> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {

      boolean acceptablePredictionError = future.get();

      if (acceptablePredictionError) {

        testModelDAO.setTestState(testID, BenchFlowTestState.TERMINATED);
        testModelDAO.setTestTerminatedState(testID, GOAL_REACHED);

        // no need to execute further
        testTasks.remove(testID);

      } else {

        testModelDAO.setTestRunningState(testID, TestRunningState.REMOVE_NON_REACHABLE_EXPERIMENTS);

        handleTestState(testID);
      }

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle  properly
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }
  }

  private void removeNonReachableExperiments(String testID) {

    logger.info("removeNonReachableExperiments with testID: " + testID);

    RemoveNonReachableExperimentsTask task = new RemoveNonReachableExperimentsTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<?> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, TestRunningState.DETERMINE_EXECUTE_EXPERIMENTS);
  }

  private void validateTerminationCriteria(String testID) {

    logger.info("validateTerminationCriteria with testID: " + testID);

    ValidateTerminationCriteria task = new ValidateTerminationCriteria(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<Boolean> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {
      boolean testComplete = future.get();

      if (testComplete) {

        testModelDAO.setTestState(testID, BenchFlowTestModel.BenchFlowTestState.TERMINATED);
        testModelDAO.setTestTerminatedState(testID, GOAL_REACHED);

        testTasks.remove(testID);

      } else {

        // update the running state
        testModelDAO.setTestRunningState(
            testID, BenchFlowTestModel.TestRunningState.DETERMINE_EXECUTE_EXPERIMENTS);

        // run the next state
        handleTestState(testID);
      }

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle  properly
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }
  }

  private synchronized void handleExperimentResult(String testID) {

    logger.info("handleExperimentResult with testID: " + testID);

    HandleExperimentResultTask task = new HandleExperimentResultTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<HandleExperimentResultTask.Result> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {

      HandleExperimentResultTask.Result result = future.get();

      switch (result) {
        case COMPLETE_SELECTION:
          testModelDAO.setTestRunningState(testID, TestRunningState.VALIDATE_TERMINATION_CRITERIA);

          handleTestState(testID);

          break;

        case CAN_REACH_GOAL:
          // TODO
          break;

        case CANNOT_REACH_GOAL:
          // TODO
          break;

        default:
          break;
      }

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle  properly
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }
  }

  private void waitForRunningTaskToComplete(
      String testID, Future future, TestRunningState nextState) {

    try {

      future.get();
      testModelDAO.setTestRunningState(testID, nextState);
      handleTestState(testID);

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle  properly
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }
  }

  public synchronized void testMaxTimeReached(String testID) {

    logger.info("testMaxTimeReached: " + testID);

    // TODO - implement me

  }
}
