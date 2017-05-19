package cloud.benchflow.experimentmanager.scheduler;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.running.CheckTerminationCriteriaTask;
import cloud.benchflow.experimentmanager.tasks.running.CheckTerminationCriteriaTask.TerminationCriteriaResult;
import cloud.benchflow.experimentmanager.tasks.running.ExecuteNewTrialTask;
import cloud.benchflow.experimentmanager.tasks.running.HandleTrialResultTask;
import cloud.benchflow.experimentmanager.tasks.running.ReExecuteTrialTask;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial.TrialStatus;
import cloud.benchflow.experimentmanager.tasks.start.StartTask;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class ExperimentTaskScheduler {

  private static Logger logger =
      LoggerFactory.getLogger(ExperimentTaskScheduler.class.getSimpleName());

  private ConcurrentMap<String, Future> experimentTasks = new ConcurrentHashMap<>();

  // TODO - running queue (1 element)

  // TODO - ready queue

  private BenchFlowExperimentModelDAO experimentModelDAO;
  private TrialModelDAO trialModelDAO;
  // TODO - should go into a stateless queue (so that we can recover)
  private ExecutorService experimentTaskExecutorService;
  private BenchFlowTestManagerService testManagerService;

  public ExperimentTaskScheduler(ExecutorService experimentTaskExecutorService) {

    this.experimentTaskExecutorService = experimentTaskExecutorService;

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
    this.testManagerService = BenchFlowExperimentManagerApplication.getTestManagerService();
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
          // TODO - we should set the state when the experiment actually has been scheduled
          setExperimentAsRunning(experimentID);
          // TODO - put test in shared (with dispatcher) ready queue
          break;

        case RUNNING:
          handleRunningState(experimentID);
          break;

        case TERMINATED:
          // TODO - remove test from running queue so dispatcher can run next test
          // experiment already executed
          logger.info("Experiment already executed. Nothing to do.");
          break;

        default:
          // no default
          break;
      }

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private void setExperimentAsRunning(String experimentID) {

    // change state to running and execute new trial
    experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.RUNNING);
    experimentModelDAO.setRunningState(experimentID, RunningState.EXECUTE_NEW_TRIAL);
    // inform test-manager that a new trial is being executed
    testManagerService.setExperimentRunningState(experimentID, RunningState.EXECUTE_NEW_TRIAL);

    handleExperimentState(experimentID);
  }

  private synchronized void handleStartState(String experimentID) {

    logger.info("handleStartState: " + experimentID);

    StartTask startTask = new StartTask(experimentID);

    Future<Boolean> future = experimentTaskExecutorService.submit(startTask);

    experimentTasks.put(experimentID, future);

    try {

      boolean deployed = future.get();

      if (deployed) {
        experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.READY);
        handleExperimentState(experimentID);
      } else {
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

  private synchronized void handleRunningState(String experimentID) {

    try {

      RunningState runningState = experimentModelDAO.getRunningState(experimentID);

      logger.info("handleRunningState: " + experimentID + " state: " + runningState.name());

      switch (runningState) {
        case EXECUTE_NEW_TRIAL:
          handleExecuteNewTrial(experimentID);
          break;

        case HANDLE_TRIAL_RESULT:
          handleTrialResult(experimentID);
          break;

        case CHECK_TERMINATION_CRITERIA:
          handleCheckTerminationCriteria(experimentID);
          break;

        case RE_EXECUTE_TRIAL:
          handleReExecuteTrial(experimentID);
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

  private void handleExecuteNewTrial(String experimentID) {

    logger.info("handleExecuteNewTrial: " + experimentID);

    ExecuteNewTrialTask newTrialTask = new ExecuteNewTrialTask(experimentID);

    Future<TrialStatus> future = experimentTaskExecutorService.submit(newTrialTask);

    experimentTasks.put(experimentID, future);

    handleExecuteTrial(experimentID, future);
  }

  private void handleReExecuteTrial(String experimentID) {

    logger.info("handleReExecuteTrial: " + experimentID);

    try {

      String trialID = experimentModelDAO.getLastExecutedTrialID(experimentID);

      ReExecuteTrialTask reExecuteTrialTask = new ReExecuteTrialTask(trialID);

      Future<TrialStatus> future = experimentTaskExecutorService.submit(reExecuteTrialTask);

      experimentTasks.put(experimentID, future);

      handleExecuteTrial(experimentID, future);

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private void handleExecuteTrial(String experimentID, Future<TrialStatus> future) {

    // TODO - change this when faban interaction changes to non-polling

    try {

      TrialStatus runStatus = future.get();

      trialModelDAO.setTrialStatus(runStatus.getTrialID(), runStatus.getStatusCode());

      experimentModelDAO.setRunningState(experimentID, RunningState.HANDLE_TRIAL_RESULT);
      testManagerService.setExperimentRunningState(experimentID, RunningState.HANDLE_TRIAL_RESULT);

      handleExperimentState(experimentID);

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private void handleTrialResult(String experimentID) {

    logger.info("handleTrialResult: " + experimentID);

    HandleTrialResultTask trialResultTask = new HandleTrialResultTask(experimentID);

    Future<Boolean> future = experimentTaskExecutorService.submit(trialResultTask);

    experimentTasks.put(experimentID, future);

    try {

      boolean checkTerminationCriteria = future.get();

      if (checkTerminationCriteria) {

        experimentModelDAO.setRunningState(experimentID,
            BenchFlowExperimentModel.RunningState.CHECK_TERMINATION_CRITERIA);
        testManagerService.setExperimentRunningState(experimentID,
            RunningState.CHECK_TERMINATION_CRITERIA);
        handleExperimentState(experimentID);

      } else {

        experimentModelDAO.setRunningState(experimentID,
            BenchFlowExperimentModel.RunningState.RE_EXECUTE_TRIAL);
        testManagerService.setExperimentRunningState(experimentID, RunningState.RE_EXECUTE_TRIAL);
        handleExperimentState(experimentID);
      }

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private void handleCheckTerminationCriteria(String experimentID) {

    logger.info("handleCheckTerminationCriteria: " + experimentID);

    CheckTerminationCriteriaTask terminationCriteriaTask =
        new CheckTerminationCriteriaTask(experimentID);

    Future<TerminationCriteriaResult> future =
        experimentTaskExecutorService.submit(terminationCriteriaTask);

    experimentTasks.put(experimentID, future);

    try {

      TerminationCriteriaResult terminationCriteriaResult = future.get();

      switch (terminationCriteriaResult) {
        case FULFILLED:
          experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
          experimentModelDAO.setTerminatedState(experimentID,
              BenchFlowExperimentModel.TerminatedState.COMPLETED);
          testManagerService.setExperimentTerminatedState(experimentID,
              BenchFlowExperimentModel.TerminatedState.COMPLETED);

          handleExperimentState(experimentID);

          break;

        case NOT_FULLFILLED:
          experimentModelDAO.setRunningState(experimentID, RunningState.EXECUTE_NEW_TRIAL);
          testManagerService.setExperimentRunningState(experimentID,
              RunningState.EXECUTE_NEW_TRIAL);
          handleExperimentState(experimentID);

          break;

        case CANNOT_BE_FULFILLED:
          experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
          experimentModelDAO.setTerminatedState(experimentID,
              BenchFlowExperimentModel.TerminatedState.FAILURE);
          testManagerService.setExperimentTerminatedState(experimentID,
              BenchFlowExperimentModel.TerminatedState.FAILURE);

          handleExperimentState(experimentID);

          break;

        default:
          // no default
          break;
      }

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}
