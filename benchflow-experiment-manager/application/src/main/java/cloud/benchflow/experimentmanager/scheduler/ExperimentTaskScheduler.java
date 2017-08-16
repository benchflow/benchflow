package cloud.benchflow.experimentmanager.scheduler;

import static cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState.READY;
import static cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState.TERMINATED;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.scheduler.running.RunningStatesHandler;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.tasks.AbortableFutureTask;
import cloud.benchflow.experimentmanager.tasks.start.StartTask;
import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public class ExperimentTaskScheduler {

  private static Logger logger =
      LoggerFactory.getLogger(ExperimentTaskScheduler.class.getSimpleName());

  private ConcurrentMap<String, AbortableFutureTask> experimentTasks = new ConcurrentHashMap<>();

  // ready queue
  private BlockingQueue<String> readyQueue = new LinkedBlockingDeque<>();

  // running queue (1 element)
  private BlockingQueue<String> runningQueue = new ArrayBlockingQueue<>(1, true);

  private BenchFlowExperimentModelDAO experimentModelDAO;
  // TODO - should go into a stateless queue (so that we can recover)
  private CustomFutureReturningExecutor experimentTaskExecutorService;
  private BenchFlowTestManagerService testManagerService;

  private RunningStatesHandler runningStatesHandler;

  public ExperimentTaskScheduler(CustomFutureReturningExecutor experimentTaskExecutorService) {
    this.experimentTaskExecutorService = experimentTaskExecutorService;
  }

  public void initialize() {

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.testManagerService = BenchFlowExperimentManagerApplication.getTestManagerService();

    // start dispatcher in separate thread
    new Thread(new ExperimentDispatcher(readyQueue, runningQueue)).start();

    this.runningStatesHandler =
        new RunningStatesHandler(experimentTasks, this, experimentTaskExecutorService);

    this.runningStatesHandler.initialize();

  }

  // used for testing
  @VisibleForTesting
  public ExecutorService getExperimentTaskExecutorService() {
    return experimentTaskExecutorService;
  }

  // used for testing
  @VisibleForTesting
  protected RunningStatesHandler getRunningStatesHandler() {
    return runningStatesHandler;
  }

  // used for testing
  @VisibleForTesting
  protected void setRunningStatesHandler(RunningStatesHandler runningStatesHandler) {
    this.runningStatesHandler = runningStatesHandler;
  }


  /**
   * Handle the Starting States of an Experiment.
   *
   * @param experimentID the experiment ID
   */
  public synchronized void handleStartingExperiment(String experimentID) {

    boolean exit = false;
    BenchFlowExperimentState experimentState;

    try {

      experimentState = experimentModelDAO.getExperimentState(experimentID);

      // Stop when we reach a final starting state
      while (!exit) {

        BenchFlowExperimentState prevTestState = experimentState;

        try {
          experimentState = handleExperimentState(experimentID);
        } catch (BenchFlowExperimentIDDoesNotExistException e) {
          e.printStackTrace();
          break;
        }

        // Exit as soon as ready or terminated is executed
        // An experiment can go to the TERMINATED state from START in case of errors
        if (prevTestState != null && experimentState != null
            && prevTestState.name().equals(experimentState.name())
            && (experimentState == READY || experimentState == TERMINATED)) {
          exit = true;
        }

      }
    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }

  }

  /**
   * Handle the Running States of an Experiment.
   *
   * @param experimentID the experiment ID
   */
  public synchronized void handleRunningExperiment(String experimentID) {

    boolean exit = false;
    BenchFlowExperimentState experimentState;

    try {
      experimentState = experimentModelDAO.getExperimentState(experimentID);

      // Stop when we reach a final state
      while (!exit) {

        BenchFlowExperimentState prevTestState = experimentState;

        try {
          experimentState = handleExperimentState(experimentID);
        } catch (BenchFlowExperimentIDDoesNotExistException e) {
          e.printStackTrace();
          break;
        }

        // Exit as soon as final state is executed
        if (prevTestState != null && experimentState != null
            && prevTestState.name().equals(experimentState.name())
            && experimentState == TERMINATED) {
          exit = true;
        }

      }

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }

  }

  private BenchFlowExperimentState handleExperimentState(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    BenchFlowExperimentState experimentState = experimentModelDAO.getExperimentState(experimentID);

    logger.info("handleExperimentSate: " + experimentID + " state: " + experimentState.name());

    switch (experimentState) {
      case START:
        handleStartState(experimentID);
        break;

      case READY:
        // put test in shared (with dispatcher) ready queue
        readyQueue.add(experimentID);
        break;

      case RUNNING:
        handleRunningExperimentState(experimentID);
        break;

      case TERMINATED:
        handleTerminatedState(experimentID);
        break;

      default:
        // no default
        break;
    }

    return experimentModelDAO.getExperimentState(experimentID);
  }

  @VisibleForTesting
  void handleStartState(String experimentID) {

    logger.info("handleStartState: " + experimentID);

    StartTask startTask = new StartTask(experimentID);

    AbortableFutureTask future =
        (AbortableFutureTask) experimentTaskExecutorService.submit(startTask);

    experimentTasks.put(experimentID, future);

    try {

      // wait for task to complete
      AbortableFutureTaskResult<Boolean> abortableFuture = getAbortableFutureTask(future);

      // return if aborted
      if (abortableFuture.isAborted()) {
        return;
      }

      boolean deployed = abortableFuture.getResult();

      if (deployed) {
        experimentModelDAO.setExperimentState(experimentID, READY);
      } else {
        experimentModelDAO.setExperimentState(experimentID, TERMINATED);
        experimentModelDAO.setTerminatedState(experimentID,
            BenchFlowExperimentModel.TerminatedState.ERROR);
        testManagerService.setExperimentTerminatedState(experimentID,
            BenchFlowExperimentModel.TerminatedState.ERROR);
      }

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle properly
      e.printStackTrace();
    }
  }

  /**
   * Handles the Running Experiment Sub-States.
   *
   * @param experimentID the experiment ID
   * @throws BenchFlowExperimentIDDoesNotExistException when the experiment ID does not exists
   */
  private void handleRunningExperimentState(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    boolean exit = false;
    BenchFlowExperimentState experimentState;

    // Stop when we reach a final state
    while (!exit) {

      try {
        experimentState = handleExperimentRunningState(experimentID);
      } catch (BenchFlowExperimentIDDoesNotExistException e) {
        e.printStackTrace();
        break;
      }

      // Exit as soon as final state is executed
      if (experimentState != null && experimentState == TERMINATED) {
        exit = true;
      }

    }

  }

  private BenchFlowExperimentState handleExperimentRunningState(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    RunningState runningState = experimentModelDAO.getRunningState(experimentID);

    logger.info("handleRunningState: " + experimentID + " state: " + runningState.name());

    testManagerService.setExperimentRunningState(experimentID, runningState);

    switch (runningState) {
      case DETERMINE_EXECUTE_TRIALS:
        runningStatesHandler.handleDetermineAndExecuteTrials(experimentID);
        break;

      case HANDLE_TRIAL_RESULT:
        runningStatesHandler.handleTrialResult(experimentID);
        break;

      case CHECK_TERMINATION_CRITERIA:
        runningStatesHandler.handleCheckTerminationCriteria(experimentID);
        break;

      default:
        // no default
        break;
    }

    return experimentModelDAO.getExperimentState(experimentID);

  }

  @VisibleForTesting
  void handleTerminatedState(String experimentID)
      throws BenchFlowExperimentIDDoesNotExistException {

    TerminatedState terminatedState = experimentModelDAO.getTerminatedState(experimentID);
    testManagerService.setExperimentTerminatedState(experimentID, terminatedState);

    // remove any tasks left
    experimentTasks.remove(experimentID);

    // remove test from running queue so dispatcher can run next experiment
    runningQueue.remove(experimentID);

  }

  /**
   * Called when user terminates experiment. <p> Given that the method is not synchronized, it has
   * not to wait that other synchronized of this class complete their execution, before getting
   * access. This because the termination should happen as soon as it is triggered from a User.
   * </p>
   *
   * @param experimentID the experiment ID
   */
  public void abortExperiment(String experimentID) {

    logger.info("abortExperiment: " + experimentID);

    try {

      BenchFlowExperimentState experimentState =
          experimentModelDAO.getExperimentState(experimentID);

      switch (experimentState) {

        case START:
          // cancel the current running task, but let it complete before
          cancelTask(experimentID);

          break;

        case READY:
          // remove from ready queue
          readyQueue.remove(experimentID);

          // cancel the current running task, but let it complete before
          cancelTask(experimentID);

          break;

        case RUNNING:

          // cancel the current running task, but let it complete before
          cancelTask(experimentID);

          break;

        case TERMINATED:
          // already terminated
          break;

        default:
          /// no default
          break;

      }


    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      // TODO - handle exception
      e.printStackTrace();
    }

  }

  private void cancelTask(String experimentID) throws BenchFlowExperimentIDDoesNotExistException {

    //Cancel the task and remove it from the testTasks queue
    Future future = experimentTasks.remove(experimentID);

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

      // set test to terminated
      experimentModelDAO.setExperimentState(experimentID, TERMINATED);
      experimentModelDAO.setTerminatedState(experimentID, TerminatedState.ABORTED);

      abortableFutureTask.abortTask();

    }
  }

  /**
   * Checks whether or not the Experiment reached the TERMINATED state.
   *
   * @param experimentID the experiment ID
   * @return true if the experiment state is terminated.
   */
  @Deprecated
  public boolean isTerminated(String experimentID) {

    try {
      BenchFlowExperimentState experimentState =
          experimentModelDAO.getExperimentState(experimentID);

      return experimentState == TERMINATED;

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      // if test is not in the DB we consider it as terminated
      return true;
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
      // if experiment has been terminated (we cancel the task in terminateTest) we stop here
      result.setAborted(true);
    } finally {
      // if experiment has been aborted (we cancel the task in terminateTest) we stop here
      if (isTaskAborted(future)) {
        result.setAborted(true);
      }
    }

    logger.info(result.toString());

    return result;
  }

  // holds the result of an abortable future
  public class AbortableFutureTaskResult<T> {

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
     * @param result the experiment result
     */
    public void setResult(T result) {
      this.result = result;
    }

    /**
     * Check if the Experiment is Aborted.
     *
     * @return true if the experiment is aborted
     */
    public boolean isAborted() {
      return aborted;
    }

    /**
     * Set the Aborted Flag.
     *
     * @param aborted experiment
     */
    public void setAborted(boolean aborted) {
      this.aborted = aborted;
    }
  }


}
