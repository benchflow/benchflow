package cloud.benchflow.testmanager.scheduler;

import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.TERMINATED;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState.*;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestTerminatedState.*;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
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
import cloud.benchflow.testmanager.tasks.start.StartTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public class TestTaskScheduler {

  private static Logger logger = LoggerFactory.getLogger(TestTaskScheduler.class.getSimpleName());

  private ConcurrentMap<String, Future> testTasks = new ConcurrentHashMap<>();

  // ready queue
  private BlockingQueue<String> readyQueue = new LinkedBlockingQueue<>();

  // running queue (1 element)
  private BlockingQueue<String> runningQueue = new ArrayBlockingQueue<String>(1, true);

  private ExecutorService taskExecutorService;
  private BenchFlowTestModelDAO testModelDAO;
  private ExplorationModelDAO explorationModelDAO;

  public TestTaskScheduler(ExecutorService taskExecutorService) {
    this.taskExecutorService = taskExecutorService;
  }

  public void initialize() {
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();

    // start dispatcher in separate thread
    new Thread(new TestDispatcher(readyQueue, runningQueue)).start();

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
          handleStartState(testID);
          break;

        case READY:
          // put test in shared (with dispatcher) ready queue
          readyQueue.add(testID);
          break;

        case RUNNING:
          // handle running states
          handleTestRunningState(testID);
          break;

        case WAITING:

          handleWaitingState(testID);
          break;

        case TERMINATED:
          // remove test from running queue so dispatcher can run next test
          runningQueue.remove(testID);
          break;

        default:
          // no default
          break;
      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private synchronized void handleStartState(String testID) {

    logger.info("handle start state: " + testID);

    StartTask startTask = new StartTask(testID);

    Future future = taskExecutorService.submit(startTask);

    testTasks.put(testID, future);

    try {

      // wait for task to complete
      future.get();

      testModelDAO.setTestState(testID, BenchFlowTestState.READY);

      handleTestState(testID);

    } catch (InterruptedException | ExecutionException | BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }

  }

  private synchronized void handleWaitingState(String testID) {

    logger.info("handle waiting state: " + testID);

    // TODO - handle received input

    try {

      // set state as ready
      testModelDAO.setTestState(testID, BenchFlowTestState.READY);

      handleTestState(testID);

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private synchronized void handleTestRunningState(String testID) {

    try {

      TestRunningState testRunningState = testModelDAO.getTestRunningState(testID);

      logger
          .info("handleTestRunningState for " + testID + " with state " + testRunningState.name());

      switch (testRunningState) {
        case DETERMINE_EXPLORATION_STRATEGY:
          determineExplorationStrategy(testID);
          break;

        case ADD_STORED_KNOWLEDGE:
          addStoredKnowledge(testID);
          break;

        case DETERMINE_EXECUTE_VALIDATION_SET:
          determineExecuteInitialValidationSet(testID);
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
          testModelDAO.setTestRunningState(testID, VALIDATE_PREDICTION_FUNCTION);
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

    waitForRunningTaskToComplete(testID, future, ADD_STORED_KNOWLEDGE);
  }

  private void addStoredKnowledge(String testID) {

    logger.info("addStoredKnowledge with testID: " + testID);

    AddStoredKnowledgeTask task = new AddStoredKnowledgeTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<?> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {

      // wait for task to complete
      future.get();

      boolean hasRegressionModel = explorationModelDAO.hasRegressionModel(testID);

      TestRunningState nextState =
          hasRegressionModel ? DETERMINE_EXECUTE_VALIDATION_SET : DETERMINE_EXECUTE_EXPERIMENTS;

      testModelDAO.setTestRunningState(testID, nextState);

      handleTestState(testID);

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle properly
      e.printStackTrace();
    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("test ID does not exist - should not happen");
    }


  }

  private void determineExecuteInitialValidationSet(String testID) {

    logger.info("determineExecuteInitialValidationSet with testID: " + testID);

    DetermineExecuteInitialValidationSetTask initialValidationSetTask =
        new DetermineExecuteInitialValidationSetTask(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future future = taskExecutorService.submit(initialValidationSetTask);

    // replace with new task
    testTasks.put(testID, future);

    waitForRunningTaskToComplete(testID, future, DETERMINE_EXECUTE_EXPERIMENTS);

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

      testModelDAO.setTestRunningState(testID, HANDLE_EXPERIMENT_RESULT);

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

    waitForRunningTaskToComplete(testID, future, VALIDATE_PREDICTION_FUNCTION);
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

        testModelDAO.setTestState(testID, TERMINATED);
        testModelDAO.setTestTerminatedState(testID, GOAL_REACHED);

        // no need to execute further
        testTasks.remove(testID);

      } else {

        testModelDAO.setTestRunningState(testID, REMOVE_NON_REACHABLE_EXPERIMENTS);

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

    waitForRunningTaskToComplete(testID, future, DETERMINE_EXECUTE_EXPERIMENTS);
  }

  private void validateTerminationCriteria(String testID) {

    logger.info("validateTerminationCriteria with testID: " + testID);

    ValidateTerminationCriteria task = new ValidateTerminationCriteria(testID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<TerminationCriteriaResult> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {

      TerminationCriteriaResult result = future.get();

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
          testModelDAO.setTestRunningState(testID, REMOVE_NON_REACHABLE_EXPERIMENTS);
          break;

        case GOAL_REACHABLE_NO_REGRESSION_EXPERIMENTS_REMAINING:
          testModelDAO.setTestRunningState(testID, DETERMINE_EXECUTE_EXPERIMENTS);
          break;

        default:
          // no default
          break;
      }

      // run the next state
      handleTestState(testID);

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
    Future<?> future = taskExecutorService.submit(task);

    // replace with new task
    testTasks.put(testID, future);

    try {

      // wait for task to complete
      future.get();

      Boolean hasRegressionModel = explorationModelDAO.hasRegressionModel(testID);

      TestRunningState nextState =
          hasRegressionModel ? DERIVE_PREDICTION_FUNCTION : VALIDATE_TERMINATION_CRITERIA;

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

  private void waitForRunningTaskToComplete(String testID, Future future,
      TestRunningState nextState) {

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
