package cloud.benchflow.testmanager.scheduler;

import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.READY;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.TERMINATED;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.WAITING;

import cloud.benchflow.dsl.definition.types.time.Time;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.TestTerminatedState;
import cloud.benchflow.testmanager.scheduler.running.RunningStatesHandler;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.tasks.AbortableFutureTask;
import cloud.benchflow.testmanager.tasks.abort.AbortRunningTestTask;
import cloud.benchflow.testmanager.tasks.start.StartTask;
import cloud.benchflow.testmanager.tasks.timeout.TimeoutTask;
import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
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
  private ConcurrentMap<String, AbortableFutureTask> testTasks = new ConcurrentHashMap<>();

  // stores the timeout task
  private ConcurrentMap<String, ScheduledFuture> timeoutTasks = new ConcurrentHashMap<>();

  // ready queue
  private BlockingQueue<String> readyQueue = new LinkedBlockingQueue<>();

  // running queue (1 element)
  private BlockingQueue<String> runningQueue = new ArrayBlockingQueue<String>(1, true);

  private CustomFutureReturningExecutor taskExecutorService;
  private ScheduledThreadPoolExecutor timeoutScheduledThreadPoolExecutor;
  private BenchFlowTestModelDAO testModelDAO;

  private RunningStatesHandler runningStatesHandler;

  private TestDispatcher testDispatcher;

  public TestTaskScheduler(CustomFutureReturningExecutor taskExecutorService,
      ScheduledThreadPoolExecutor timeoutScheduledThreadPoolExecutor) {
    this.taskExecutorService = taskExecutorService;
    this.timeoutScheduledThreadPoolExecutor = timeoutScheduledThreadPoolExecutor;
  }

  public void initialize() {
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();

    // start dispatcher in separate thread
    new Thread(testDispatcher = new TestDispatcher(readyQueue, runningQueue)).start();

    this.runningStatesHandler = new RunningStatesHandler(testTasks, taskExecutorService, this);
    this.runningStatesHandler.initialize();

  }

  @VisibleForTesting
  public TestDispatcher getTestDispatcher() {
    return testDispatcher;
  }

  // used for testing
  @VisibleForTesting
  public CustomFutureReturningExecutor getTaskExecutorService() {
    return taskExecutorService;
  }

  // used for testing
  @VisibleForTesting
  public RunningStatesHandler getRunningStatesHandler() {
    return runningStatesHandler;
  }

  // used for testing
  @VisibleForTesting
  public void setRunningStatesHandler(RunningStatesHandler runningStatesHandler) {
    this.runningStatesHandler = runningStatesHandler;
  }

  // used for testing
  @VisibleForTesting
  protected ConcurrentMap<String, ScheduledFuture> getTimeoutTasks() {
    return timeoutTasks;
  }

  // used for testing
  @VisibleForTesting
  protected ScheduledThreadPoolExecutor getTimeoutScheduledThreadPoolExecutor() {
    return timeoutScheduledThreadPoolExecutor;
  }

  /**
   * Handle the Starting States of a Test.
   *
   * @param testID the test ID
   */
  public synchronized void handleStartingTest(String testID) {

    logger.info("handleStartingTest for " + testID);

    boolean exit = false;
    BenchFlowTestState testState;

    try {

      testState = testModelDAO.getTestState(testID);

      // Stop when we reach a final starting state
      while (!exit) {

        BenchFlowTestState prevTestState = testState;

        try {
          testState = handleTestState(testID);
        } catch (BenchFlowTestIDDoesNotExistException e) {
          e.printStackTrace();
          break;
        }

        logger.info("handleStartingTest: prevTestState == " + prevTestState);
        logger.info("handleStartingTest: testState == " + testState);

        // Exit as soon as ready is executed, or the test has been terminated
        if (prevTestState == testState && (testState == READY || testState == TERMINATED)) {
          exit = true;
        }

      }
    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }

  }

  /**
   * Handle the Running States of a Test.
   *
   * @param testID the test ID
   */
  public synchronized void handleRunningTest(String testID) {

    logger.info("handleRunningTest for " + testID);

    boolean exit = false;
    BenchFlowTestState testState;
    BenchFlowTestState prevTestState;
    TestRunningState testRunningState;
    TestRunningState prevTestRunningState;

    try {

      testState = testModelDAO.getTestState(testID);
      testRunningState = testModelDAO.getTestRunningState(testID);

      // Stop when we reach a final state
      while (!exit) {

        prevTestState = testState;
        prevTestRunningState = testRunningState;

        try {
          testState = handleTestState(testID);
        } catch (BenchFlowTestIDDoesNotExistException e) {
          e.printStackTrace();
          break;
        }

        testRunningState = testModelDAO.getTestRunningState(testID);
        logger.info("handleRunningTest: prevTestState == " + prevTestState);
        logger.info("handleRunningTest: testState == " + testState);
        logger.info("handleRunningTest: testRunningState == " + testRunningState);
        logger.info("handleRunningTest: prevTestRunningState == " + prevTestRunningState);

        // Exit as soon as final state is executed
        if (prevTestState == testState && (testState == WAITING || testState == TERMINATED)) {
          exit = true;
        }

        /*
          Case HANDLE_EXPERIMENT_RESULT:
          Exit while waiting for the Experiment Manager to notify about the scheduled
          experiment to be executed. This exits before the execution of HANDLE_EXPERIMENT_RESULT
        
          Case TERMINATING:
          Check depends if the system is waiting for input from the ExperimentManager or not. In
          the inner loop for running experiment we exit, but in the outer loop (HERE) we check if
          we expect a result from the ExperimentManager or not. If we do not expect it we run the
          loop again so that the TERMINATED state handling gets executed, otherwise we wait for
          the Experiment result to trigger the TERMINATED handling.
         */
        else if (testRunningState == TestRunningState.HANDLE_EXPERIMENT_RESULT
            || testRunningState == TestRunningState.TERMINATING
                && (prevTestRunningState == TestRunningState.HANDLE_EXPERIMENT_RESULT
                    || prevTestRunningState == TestRunningState.DETERMINE_EXECUTE_VALIDATION_SET)) {
          exit = true;
        }

      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }

  }

  private BenchFlowTestState handleTestState(String testID)
      throws BenchFlowTestIDDoesNotExistException {

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
        handleRunningTestState(testID);
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

    return testModelDAO.getTestState(testID);
  }

  private void handleStartState(String testID) {

    logger.info("handle start state: " + testID);

    StartTask startTask = new StartTask(testID);

    AbortableFutureTask future = (AbortableFutureTask) taskExecutorService.submit(startTask);

    testTasks.put(testID, future);

    try {

      // wait for task to complete
      if (getAbortableFutureTask(future).isAborted()) {
        return;
      }

      testModelDAO.setTestState(testID, READY);

    } catch (InterruptedException | ExecutionException | BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }

  }

  private void handleWaitingState(String testID) {

    logger.info("handle waiting state: " + testID);

    // TODO - handle received input

    try {

      // set state as ready
      testModelDAO.setTestState(testID, READY);

      // update max running time timeout
      ScheduledFuture timeoutTaskFuture = timeoutTasks.remove(testID);

      // cancel max running time timeout
      timeoutTaskFuture.cancel(true);

      long delay = timeoutTaskFuture.getDelay(TimeUnit.SECONDS);
      Time remainingMaxRunningTime =
          new Time(Duration.of(delay, ChronoUnit.SECONDS), ChronoUnit.SECONDS);

      testModelDAO.setMaxRunTime(testID, remainingMaxRunningTime);

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles the Running Test Sub-States.
   *
   * @param testID the test ID
   * @throws BenchFlowTestIDDoesNotExistException when the test ID does not exists
   */
  private void handleRunningTestState(String testID) throws BenchFlowTestIDDoesNotExistException {

    logger.info("handleRunningTestState for " + testID);

    boolean exit = false;
    BenchFlowTestState testState;
    TestRunningState testRunningState;

    // set timeout if not already set, and a timeout is declared in the model
    setTimeoutIfNeeded(testID);

    // Stop when we reach a final state
    while (!exit) {

      try {
        testState = handleTestRunningState(testID);
      } catch (BenchFlowTestIDDoesNotExistException e) {
        e.printStackTrace();
        break;
      }

      testRunningState = testModelDAO.getTestRunningState(testID);
      logger.info("handleRunningTestState: testState == " + testState);
      logger.info("handleRunningTestState: testRunningState == " + testRunningState);

      // Exit as soon as final state is executed
      if (testState == WAITING || testState == TERMINATED) {
        exit = true;
      }

      /*
        Case HANDLE_EXPERIMENT_RESULT:
        Exit while waiting for the Experiment Manager to notify about the scheduled
        experiment to be executed. This exits before the execution of HANDLE_EXPERIMENT_RESULT
      
        Case TERMINATING:
        Check depends if the system is waiting for input from the ExperimentManager or not. In
        the inner (HERE) loop for running experiment we exit, but in the outer loop we check if
        we expect a result from the ExperimentManager or not. If we do not expect it we run the
        loop again so that the TERMINATED state handling gets executed, otherwise we wait for
        the Experiment result to trigger the TERMINATED handling.
      */
      else if (testRunningState == TestRunningState.HANDLE_EXPERIMENT_RESULT
          || testRunningState == TestRunningState.TERMINATING) {
        exit = true;
      }

    }

  }

  private void setTimeoutIfNeeded(String testID) throws BenchFlowTestIDDoesNotExistException {

    if (testModelDAO.hasMaxRunningTime(testID) && !timeoutTasks.containsKey(testID)) {
      Time maxRunTime = testModelDAO.getMaxRunningTime(testID);
      TimeoutTask timeoutTask = new TimeoutTask(testID, this);
      ScheduledFuture<?> timeoutFuture = timeoutScheduledThreadPoolExecutor.schedule(timeoutTask,
          maxRunTime.toSecondsPart(), TimeUnit.SECONDS);
      timeoutTasks.put(testID, timeoutFuture);
    }
  }

  private BenchFlowTestState handleTestRunningState(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    try {

      TestRunningState testRunningState = testModelDAO.getTestRunningState(testID);

      logger
          .info("handleTestRunningState for " + testID + " with state " + testRunningState.name());

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
          runningStatesHandler.derivePredictionFunction(testID);
          break;

        case VALIDATE_PREDICTION_FUNCTION:
          runningStatesHandler.validatePredictionFunction(testID);
          break;

        case REMOVE_NON_REACHABLE_EXPERIMENTS:
          runningStatesHandler.removeNonReachableExperiments(testID);
          break;

        case TERMINATING:
          runningStatesHandler.terminating(testID);
          break;

        default:
          break;
      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }

    return testModelDAO.getTestState(testID);
  }

  @VisibleForTesting
  public void handleTerminatedState(String testID) {

    logger.info("handleTerminatedState for " + testID);

    // remove max running time timeout, if present
    ScheduledFuture timeoutTaskFuture = timeoutTasks.remove(testID);
    if (timeoutTaskFuture != null) {
      timeoutTaskFuture.cancel(true);
    }

    // remove any tasks left
    testTasks.remove(testID);

    // remove test from running queue so dispatcher can run next test
    runningQueue.remove(testID);
  }


  /**
   * Called when user terminates test or max run time has been reached. <p> Given that the method is
   * not synchronized, it has not to wait that other synchronized of this class complete their
   * execution, before getting access. This because the termination should happen as soon as it is
   * triggered from a Timeout or a User. </p>
   *
   * @param testID the test ID
   */
  public void terminateTest(String testID) {

    logger.info("terminateTest: " + testID);

    try {

      BenchFlowTestState testState = testModelDAO.getTestState(testID);

      logger.info("Current status: " + testState);

      switch (testState) {
        case READY:
          // remove from ready queue
          readyQueue.remove(testID);

          // cancel the current running task, but let it complete before
          cancelTask(testID);

          // set to terminated
          testModelDAO.setTestState(testID, TERMINATED);
          testModelDAO.setTestTerminatedState(testID, TestTerminatedState.PARTIALLY_COMPLETE);

          break;

        case WAITING:
        case START:
          // cancel the current running task, but let it complete before
          cancelTask(testID);

          // set to terminated
          testModelDAO.setTestState(testID, TERMINATED);
          testModelDAO.setTestTerminatedState(testID, TestTerminatedState.PARTIALLY_COMPLETE);

          break;

        case RUNNING:

          TestRunningState runningState = testModelDAO.getTestRunningState(testID);

          // set to TERMINATING
          testModelDAO.setTestRunningState(testID, TestRunningState.TERMINATING);

          // cancel the current running task, but let it complete before
          cancelTask(testID);

          if (runningState == TestRunningState.HANDLE_EXPERIMENT_RESULT
              || runningState == TestRunningState.DETERMINE_EXECUTE_VALIDATION_SET) {

            logger.info("Need to execute AbortRunningTask");

            // if an experiment is running we cancel it on the experiment-manager
            // We use a task because we don't want to keep the lock on the TestTaskScheduler
            // longer than necessary
            AbortRunningTestTask abortRunningTestTask = new AbortRunningTestTask(testID);
            Future abortRunningTestTaskFuture = taskExecutorService.submit(abortRunningTestTask);

            try {

              abortRunningTestTaskFuture.get();

            } catch (InterruptedException | ExecutionException e) {
              // nothing to do
              e.printStackTrace();
            }

          } else {
            // run the TERMINATING state to terminate
            handleRunningTest(testID);
          }

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

  private void cancelTask(String testID) throws BenchFlowTestIDDoesNotExistException {

    //Cancel the task and remove it from the testTasks queue
    Future future = testTasks.remove(testID);

    AbortableFutureTask abortableFutureTask = null;

    if (future instanceof AbortableFutureTask) {
      abortableFutureTask = (AbortableFutureTask) future;

    } else {
      //TODO - Handle it properly (should never happen)
      new Exception("An Unexpected Task Type is Scheduled in CustomFutureReturningExecutor")
          .printStackTrace();
    }

    //Let the task complete before interrupting it
    if (abortableFutureTask != null) {

      // NOTE: enable if we need to cancel the executing task
      //      abortableFutureTask.cancel(false);

      abortableFutureTask.abortTask();

    }
  }

  private boolean isTaskAborted(AbortableFutureTask future) {

    return future.isAborted();

  }

  /**
   * Get the Abortable Future Task from a Future.
   *
   * @param future the AbortableFutureTask future
   * @return AbortableFutureTaskResult
   * @throws InterruptedException exception
   * @throws ExecutionException exception
   */
  public AbortableFutureTaskResult getAbortableFutureTask(AbortableFutureTask future)
      throws InterruptedException, ExecutionException {

    AbortableFutureTaskResult result = new AbortableFutureTaskResult();

    try {
      Object futureResult = future.get();
      result.setResult(futureResult);
    } catch (CancellationException e) {
      // if test has been terminated (we cancel the task in terminateTest) we stop here
      result.setAborted(true);
    } finally {
      // if test has been aborted (we cancel the task in terminateTest) we stop here
      if (isTaskAborted(future)) {
        result.setAborted(true);
      }
    }

    logger.info(result.toString());

    return result;
  }

  /**
   * Checks whether or not the Test reached the TERMINATED state.
   *
   * @param testID the test ID
   * @return true if the test state is terminated.
   */
  @Deprecated
  public boolean isTerminated(String testID) {

    try {
      BenchFlowTestState benchFlowTestState = testModelDAO.getTestState(testID);

      return benchFlowTestState == TERMINATED;

    } catch (BenchFlowTestIDDoesNotExistException e) {
      // if test is not in the DB we consider it as terminated
      return true;
    }

  }

  // holds the result of an abortable future
  public class AbortableFutureTaskResult<T> {

    // NOTE: currenlty the result is not used
    private T result = null;
    private boolean aborted = false;

    public T getResult() {
      // the result is only valid if the task was not aborted
      if (!aborted) {
        return result;
      }

      return null;
    }

    /**
     * Set the Result.
     *
     * @param result the test result
     */
    public void setResult(T result) {
      this.result = result;
    }

    /**
     * Check if the Test is Aborted.
     *
     * @return true if the test is aborted
     */
    public boolean isAborted() {
      return aborted;
    }

    /**
     * Set the Aborted Flab.
     *
     * @param aborted test
     */
    public void setAborted(boolean aborted) {
      this.aborted = aborted;
    }
  }
}
