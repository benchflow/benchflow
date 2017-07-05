package cloud.benchflow.experimentmanager.scheduler;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.scheduler.running.RunningStatesHandler;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.tasks.start.StartTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
 */
public class ExperimentTaskScheduler {

  private static Logger logger =
      LoggerFactory.getLogger(ExperimentTaskScheduler.class.getSimpleName());

  private ConcurrentMap<String, Future> experimentTasks = new ConcurrentHashMap<>();

  // ready queue
  private BlockingQueue<String> readyQueue = new LinkedBlockingDeque<>();

  // running queue (1 element)
  private BlockingQueue<String> runningQueue = new ArrayBlockingQueue<>(1, true);

  private BenchFlowExperimentModelDAO experimentModelDAO;
  // TODO - should go into a stateless queue (so that we can recover)
  private ExecutorService experimentTaskExecutorService;
  private BenchFlowTestManagerService testManagerService;

  private RunningStatesHandler runningStatesHandler;

  public ExperimentTaskScheduler(ExecutorService experimentTaskExecutorService) {
    this.experimentTaskExecutorService = experimentTaskExecutorService;
  }

  public void initialize() {

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.testManagerService = BenchFlowExperimentManagerApplication.getTestManagerService();

    this.runningStatesHandler =
        new RunningStatesHandler(experimentTasks, this, experimentTaskExecutorService);

    // start dispatcher in separate thread
    new Thread(new ExperimentDispatcher(readyQueue, runningQueue)).start();

  }

  // used for testing
  public ExecutorService getExperimentTaskExecutorService() {
    return experimentTaskExecutorService;
  }

  public synchronized void handleExperimentState(String experimentID) {

    try {

      BenchFlowExperimentState experimentState =
          experimentModelDAO.getExperimentState(experimentID);

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
          handleRunningState(experimentID);
          break;

        case TERMINATED:
          handleTerminatedState(experimentID);
          break;

        default:
          // no default
          break;
      }

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private void handleStartState(String experimentID) {

    logger.info("handleStartState: " + experimentID);

    StartTask startTask = new StartTask(experimentID);

    Future<Boolean> future = experimentTaskExecutorService.submit(startTask);

    experimentTasks.put(experimentID, future);

    try {

      boolean deployed = future.get();

      if (isTerminated(experimentID)) {
        // if test has been terminated we stop here
        return;
      }

      if (deployed) {
        experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.READY);
        handleExperimentState(experimentID);
      } else {
        experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
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

  private void handleRunningState(String experimentID) {

    try {

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

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      // should not happen since it was added earlier
      logger.error("experiment ID does not exist - should not happen");
      e.printStackTrace();
    }
  }

  private void handleTerminatedState(String experimentID) {

    try {

      TerminatedState terminatedState = experimentModelDAO.getTerminatedState(experimentID);
      testManagerService.setExperimentTerminatedState(experimentID, terminatedState);

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }

    // remove any tasks left
    experimentTasks.remove(experimentID);

    // remove test from running queue so dispatcher can run next experiment
    runningQueue.remove(experimentID);

  }



  public synchronized void abortExperiment(String experimentID) {

    logger.info("abortExperiment: " + experimentID);

    try {

      BenchFlowExperimentState experimentState =
          experimentModelDAO.getExperimentState(experimentID);

      switch (experimentState) {

        case START:
          // set to terminated
          experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
          experimentModelDAO.setTerminatedState(experimentID, TerminatedState.ABORTED);
          handleExperimentState(experimentID);

        case READY:
          // remove from ready queue
          readyQueue.remove(experimentID);
          // set to terminated
          experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
          experimentModelDAO.setTerminatedState(experimentID, TerminatedState.ABORTED);
          handleExperimentState(experimentID);
          break;

        case RUNNING:

          // set test to terminated
          experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
          experimentModelDAO.setTerminatedState(experimentID, TerminatedState.ABORTED);

          // First we wait for any currently running tasks to finish since they finish quickly.
          // Since we already set the state to TERMINATED the task will not continue
          // to the next state.
          Future runningTaskFuture = experimentTasks.get(experimentID);
          if (runningTaskFuture != null) {
            try {
              runningTaskFuture.get();
            } catch (InterruptedException | ExecutionException e) {
              // nothing to do
            }
          }

          handleExperimentState(experimentID);

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

  public boolean isTerminated(String experimentID) {

    try {
      BenchFlowExperimentState experimentState =
          experimentModelDAO.getExperimentState(experimentID);

      return experimentState == BenchFlowExperimentState.TERMINATED;

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      // if test is not in the DB we consider it as terminated
      return true;
    }

  }



}
