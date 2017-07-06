package cloud.benchflow.testmanager.scheduler;

import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState.DETERMINE_EXECUTE_VALIDATION_SET;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState.HANDLE_EXPERIMENT_RESULT;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState.VALIDATE_PREDICTION_FUNCTION;

import cloud.benchflow.dsl.definition.types.time.Time;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestTerminatedState;
import cloud.benchflow.testmanager.scheduler.running.RunningStatesHandler;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.tasks.abort.AbortRunningTestTask;
import cloud.benchflow.testmanager.tasks.start.StartTask;
import cloud.benchflow.testmanager.tasks.timeout.TimeoutTask;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public class TestTaskScheduler {

  private static Logger logger = LoggerFactory.getLogger(TestTaskScheduler.class.getSimpleName());

  // stores the currently running test tasks
  private ConcurrentMap<String, Future> testTasks = new ConcurrentHashMap<>();

  // stores the timeout task
  private ConcurrentMap<String, ScheduledFuture> timeoutTasks = new ConcurrentHashMap<>();

  // ready queue
  private BlockingQueue<String> readyQueue = new LinkedBlockingQueue<>();

  // running queue (1 element)
  private BlockingQueue<String> runningQueue = new ArrayBlockingQueue<String>(1, true);

  private ExecutorService taskExecutorService;
  private ScheduledThreadPoolExecutor timeoutScheduledThreadPoolExecutor;
  private BenchFlowTestModelDAO testModelDAO;

  private RunningStatesHandler runningStatesHandler;

  public TestTaskScheduler(ExecutorService taskExecutorService,
      ScheduledThreadPoolExecutor timeoutScheduledThreadPoolExecutor) {
    this.taskExecutorService = taskExecutorService;
    this.timeoutScheduledThreadPoolExecutor = timeoutScheduledThreadPoolExecutor;
  }

  public void initialize() {
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();

    // start dispatcher in separate thread
    new Thread(new TestDispatcher(readyQueue, runningQueue)).start();

    this.runningStatesHandler = new RunningStatesHandler(testTasks, taskExecutorService, this);
    this.runningStatesHandler.initialize();

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
          handleTerminatedState(testID);
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

      // update max running time timeout
      ScheduledFuture timeoutTaskFuture = timeoutTasks.remove(testID);

      long delay = timeoutTaskFuture.getDelay(TimeUnit.SECONDS);
      Time remainingMaxRunningTime =
          new Time(Duration.of(delay, ChronoUnit.SECONDS), ChronoUnit.SECONDS);

      testModelDAO.setMaxRunTime(testID, remainingMaxRunningTime);

      // cancel max running time timeout
      timeoutTaskFuture.cancel(true);

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

      // set timeout if not already set
      if (!timeoutTasks.containsKey(testID)) {
        Time maxRunTime = testModelDAO.getMaxRunningTime(testID);
        TimeoutTask timeoutTask = new TimeoutTask(testID, this);
        ScheduledFuture<?> timeoutFuture = timeoutScheduledThreadPoolExecutor.schedule(timeoutTask,
            maxRunTime.toSecondsPart(), TimeUnit.SECONDS);
        timeoutTasks.put(testID, timeoutFuture);
      }

      switch (testRunningState) {
        case DETERMINE_EXPLORATION_STRATEGY:
          runningStatesHandler.determineExplorationStrategy(testID);
          break;

        case ADD_STORED_KNOWLEDGE:
          runningStatesHandler.addStoredKnowledge(testID);
          break;

        case DETERMINE_EXECUTE_VALIDATION_SET:
          runningStatesHandler.determineExecuteInitialValidationSet(testID);
          break;

        case DETERMINE_EXECUTE_EXPERIMENTS:
          runningStatesHandler.determineAndExecuteExperiments(testID);
          break;

        case HANDLE_EXPERIMENT_RESULT:
          runningStatesHandler.handleExperimentResult(testID);
          break;

        case VALIDATE_TERMINATION_CRITERIA:
          runningStatesHandler.validateTerminationCriteria(testID);
          break;

        case DERIVE_PREDICTION_FUNCTION:
          testModelDAO.setTestRunningState(testID, VALIDATE_PREDICTION_FUNCTION);
          runningStatesHandler.derivePredictionFunction(testID);
          break;

        case VALIDATE_PREDICTION_FUNCTION:
          runningStatesHandler.validatePredictionFunction(testID);
          break;

        case REMOVE_NON_REACHABLE_EXPERIMENTS:
          runningStatesHandler.removeNonReachableExperiments(testID);
          break;

        default:
          break;
      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private void handleTerminatedState(String testID) {
    // remove max running time timeout
    ScheduledFuture timeoutTaskFuture = timeoutTasks.remove(testID);
    timeoutTaskFuture.cancel(true);

    // remove any tasks left
    testTasks.remove(testID);

    // remove test from running queue so dispatcher can run next test
    runningQueue.remove(testID);
  }


  /**
   * Called when user terminates test or max run time has been reached.
   */
  public synchronized void terminateTest(String testID) {

    logger.info("terminateTest: " + testID);

    try {

      BenchFlowTestState testState = testModelDAO.getTestState(testID);

      switch (testState) {
        case READY:
          // remove from ready queue
          readyQueue.remove(testID);
          // set to terminated
          testModelDAO.setTestState(testID, BenchFlowTestState.TERMINATED);
          testModelDAO.setTestTerminatedState(testID, TestTerminatedState.PARTIALLY_COMPLETE);
          handleTestState(testID);
          break;

        case WAITING:
        case START:
          // set to terminated
          testModelDAO.setTestState(testID, BenchFlowTestState.TERMINATED);
          testModelDAO.setTestTerminatedState(testID, TestTerminatedState.PARTIALLY_COMPLETE);
          handleTestState(testID);
          break;

        case RUNNING:

          TestRunningState runningState = testModelDAO.getTestRunningState(testID);

          // set test to terminated
          testModelDAO.setTestState(testID, BenchFlowTestState.TERMINATED);
          testModelDAO.setTestTerminatedState(testID, TestTerminatedState.PARTIALLY_COMPLETE);

          // First we wait for any currently running tasks to finish since they finish quickly.
          // Since we already set the test state to TERMINATED the task will not continue
          // to the next state.
          Future runningTaskFuture = testTasks.get(testID);
          if (runningTaskFuture != null) {
            try {
              runningTaskFuture.get();
            } catch (InterruptedException | ExecutionException e) {
              // nothing to do
            }
          }

          if (runningState == HANDLE_EXPERIMENT_RESULT
              || runningState == DETERMINE_EXECUTE_VALIDATION_SET) {

            // if an experiment is running we cancel it on the experiment-manager
            AbortRunningTestTask abortRunningTestTask = new AbortRunningTestTask(testID);
            Future abortRunningTestTaskFuture = taskExecutorService.submit(abortRunningTestTask);

            try {
              abortRunningTestTaskFuture.get();
            } catch (InterruptedException | ExecutionException e) {
              // nothing to do
            }

          }

          // once no running tasks we handle the TERMINATED state as usual
          handleTestState(testID);
          break;

        case TERMINATED:
          // already terminated
          break;

        default:
          // no default
          break;
      }


    } catch (BenchFlowTestIDDoesNotExistException e) {
      // TODO - handle me
      e.printStackTrace();
    }

  }
}
