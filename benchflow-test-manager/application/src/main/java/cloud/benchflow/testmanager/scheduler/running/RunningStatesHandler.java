package cloud.benchflow.testmanager.scheduler.running;

import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.TERMINATED;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestTerminatedState.COMPLETED_WITH_FAILURE;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestTerminatedState.GOAL_REACHED;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState;
import cloud.benchflow.testmanager.scheduler.CustomFutureReturningExecutor;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler.AbortableFutureTaskResult;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.tasks.AbortableFutureTask;
import cloud.benchflow.testmanager.tasks.running.AddStoredKnowledgeTask;
import cloud.benchflow.testmanager.tasks.running.DerivePredictionFunctionTask;
import cloud.benchflow.testmanager.tasks.running.DetermineExecuteExperimentsTask;
import cloud.benchflow.testmanager.tasks.running.DetermineExecuteInitialValidationSetTask;
import cloud.benchflow.testmanager.tasks.running.DetermineExplorationStrategyTask;
import cloud.benchflow.testmanager.tasks.running.HandleExperimentResultTask;
import cloud.benchflow.testmanager.tasks.running.RemoveNonReachableExperimentsTask;
import cloud.benchflow.testmanager.tasks.running.ValidatePredictionFunctionTask;
import cloud.benchflow.testmanager.tasks.running.ValidateTerminationCriteria;
import cloud.benchflow.testmanager.tasks.running.ValidateTerminationCriteria.TerminationCriteriaResult;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-02
 * @author vincenzoferme
 */
public class RunningStatesHandler {

  /**
   * Every running state checks if the test has been terminated after each task
   * to avoid that the test state is overwritten.
   */

  private static Logger logger =
      LoggerFactory.getLogger(RunningStatesHandler.class.getSimpleName());

  private ConcurrentMap<String, AbortableFutureTask> testTasks;

  private CustomFutureReturningExecutor taskExecutorService;

  private ExplorationModelDAO explorationModelDAO;
  private BenchFlowTestModelDAO testModelDAO;

  private TestTaskScheduler testTaskScheduler;

  public RunningStatesHandler(ConcurrentMap<String, AbortableFutureTask> testTasks,
      CustomFutureReturningExecutor taskExecutorService, TestTaskScheduler testTaskScheduler) {

    this.testTasks = testTasks;
    this.taskExecutorService = taskExecutorService;
    this.testTaskScheduler = testTaskScheduler;
  }

  public void initialize() {

    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();

  }

  public void determineExplorationStrategy(String testID) {

    logger.info("determineExplorationStrategy with testID: " + testID);

    DetermineExplorationStrategyTask task = new DetermineExplorationStrategyTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    AbortableFutureTask<?> future = (AbortableFutureTask) taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, TestRunningState.ADD_STORED_KNOWLEDGE);
  }

  public void addStoredKnowledge(String testID) {

    logger.info("addStoredKnowledge with testID: " + testID);

    AddStoredKnowledgeTask task = new AddStoredKnowledgeTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    AbortableFutureTask<?> future = (AbortableFutureTask) taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {

      // wait for task to complete
      if (testTaskScheduler.getAbortableFutureTask(future).isAborted()) {
        logger.info("Task has been aborted for test: " + testID);
        return;
      }

      boolean hasRegressionModel = explorationModelDAO.hasRegressionModel(testID);

      TestRunningState nextState =
          hasRegressionModel ? TestRunningState.DETERMINE_EXECUTE_VALIDATION_SET
              : TestRunningState.DETERMINE_EXECUTE_EXPERIMENTS;

      testModelDAO.setTestRunningState(testID, nextState);

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle properly
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }


  }

  public void determineExecuteInitialValidationSet(String testID) {

    logger.info("determineExecuteInitialValidationSet with testID: " + testID);

    DetermineExecuteInitialValidationSetTask initialValidationSetTask =
        new DetermineExecuteInitialValidationSetTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    AbortableFutureTask future =
        (AbortableFutureTask) taskExecutorService.submit(initialValidationSetTask);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, TestRunningState.DETERMINE_EXECUTE_EXPERIMENTS);

  }

  public void determineAndExecuteExperiments(String testID) {

    logger.info("determineAndExecuteExperiments with testID: " + testID);

    DetermineExecuteExperimentsTask task = new DetermineExecuteExperimentsTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    AbortableFutureTask<?> future = (AbortableFutureTask) taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, TestRunningState.HANDLE_EXPERIMENT_RESULT);

    // TODO: decide if it is actually needed
    //    // we don't wait for the task to complete since the experiment-manager
    //    // will send the result
    //    try {
    //
    //      testModelDAO.setTestRunningState(testID, HANDLE_EXPERIMENT_RESULT);
    //
    //    } catch (BenchFlowTestIDDoesNotExistException e) {
    //      // should not happen since it was added earlier
    //      logger.error("test ID does not exist - should not happen");
    //    }
  }

  public void derivePredictionFunction(String testID) {

    logger.info("derivePredictionFunction with testID: " + testID);

    DerivePredictionFunctionTask task = new DerivePredictionFunctionTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    AbortableFutureTask<?> future = (AbortableFutureTask) taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, TestRunningState.VALIDATE_PREDICTION_FUNCTION);
  }

  public void validatePredictionFunction(String testID) {

    logger.info("validatePredictionFunction with testID: " + testID);

    ValidatePredictionFunctionTask task = new ValidatePredictionFunctionTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    AbortableFutureTask<Boolean> future = (AbortableFutureTask) taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {

      // TODO - update: set next state as validate termination criteria

      boolean acceptablePredictionError = false;

      // wait for task to complete
      AbortableFutureTaskResult<Boolean> futureResult =
          testTaskScheduler.getAbortableFutureTask(future);

      if (futureResult.isAborted()) {
        logger.info("Task has been aborted for test: " + testID);
        return;
      } else {
        acceptablePredictionError = futureResult.getResult();
      }

      if (acceptablePredictionError) {

        testModelDAO.setTestState(testID, TERMINATED);
        testModelDAO.setTestTerminatedState(testID, GOAL_REACHED);

      } else {

        testModelDAO.setTestRunningState(testID, TestRunningState.REMOVE_NON_REACHABLE_EXPERIMENTS);

      }

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle  properly
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }
  }

  public void removeNonReachableExperiments(String testID) {

    logger.info("removeNonReachableExperiments with testID: " + testID);

    RemoveNonReachableExperimentsTask task = new RemoveNonReachableExperimentsTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    AbortableFutureTask<?> future = (AbortableFutureTask) taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, TestRunningState.DETERMINE_EXECUTE_EXPERIMENTS);
  }

  public void validateTerminationCriteria(String testID) {

    logger.info("validateTerminationCriteria with testID: " + testID);

    ValidateTerminationCriteria task = new ValidateTerminationCriteria(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    AbortableFutureTask<TerminationCriteriaResult> future =
        (AbortableFutureTask) taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {

      TerminationCriteriaResult result;

      AbortableFutureTaskResult<TerminationCriteriaResult> futureResult =
          testTaskScheduler.getAbortableFutureTask(future);

      if (futureResult.isAborted()) {
        logger.info("Task has been aborted for test: " + testID);
        return;
      } else {
        result = futureResult.getResult();
      }

      switch (result) {

        case GOAL_NOT_REACHABLE:
          testModelDAO.setTestState(testID, TERMINATED);
          testModelDAO.setTestTerminatedState(testID, COMPLETED_WITH_FAILURE);
          break;

        case GOAL_REACHABLE_REGRESSION_PREDICTION_ACCEPTABLE:
        case GOAL_REACHABLE_NO_REGRESSION_EXPERIMENTS_EXECUTED:
          testModelDAO.setTestState(testID, TERMINATED);
          testModelDAO.setTestTerminatedState(testID, GOAL_REACHED);
          break;

        case GOAL_REACHABLE_REGRESSION_PREDICTION_NOT_ACCEPTABLE:
          testModelDAO.setTestRunningState(testID,
              TestRunningState.REMOVE_NON_REACHABLE_EXPERIMENTS);
          break;

        case GOAL_REACHABLE_NO_REGRESSION_EXPERIMENTS_REMAINING:
          testModelDAO.setTestRunningState(testID, TestRunningState.DETERMINE_EXECUTE_EXPERIMENTS);
          break;

        default:
          // no default
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

  public synchronized void handleExperimentResult(String testID) {

    logger.info("handleExperimentResult with testID: " + testID);

    HandleExperimentResultTask task = new HandleExperimentResultTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    AbortableFutureTask<?> future = (AbortableFutureTask) taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {

      // wait for task to complete
      if (testTaskScheduler.getAbortableFutureTask(future).isAborted()) {
        logger.info("Task has been aborted for test: " + testID);
        return;
      }

      Boolean hasRegressionModel = explorationModelDAO.hasRegressionModel(testID);

      TestRunningState nextState = hasRegressionModel ? TestRunningState.DERIVE_PREDICTION_FUNCTION
          : TestRunningState.VALIDATE_TERMINATION_CRITERIA;

      testModelDAO.setTestRunningState(testID, nextState);

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle  properly
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }
  }

  private void waitForRunningTaskToComplete(String testID, AbortableFutureTask future,
      TestRunningState nextState) {

    try {

      // wait for task to complete
      if (testTaskScheduler.getAbortableFutureTask(future).isAborted()) {
        logger.info("Task has been aborted for test: " + testID);
        return;
      }

      testModelDAO.setTestRunningState(testID, nextState);

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle  properly
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }
  }

}
