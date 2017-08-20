package cloud.benchflow.experimentmanager.scheduler.running;

import static cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState.TERMINATED;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.models.TrialModel.TrialStatus;
import cloud.benchflow.experimentmanager.scheduler.CustomFutureReturningExecutor;
import cloud.benchflow.experimentmanager.scheduler.ExperimentTaskScheduler;
import cloud.benchflow.experimentmanager.scheduler.ExperimentTaskScheduler.AbortableFutureTaskResult;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.AbortableFutureTask;
import cloud.benchflow.experimentmanager.tasks.running.CheckTerminationCriteriaTask;
import cloud.benchflow.experimentmanager.tasks.running.CheckTerminationCriteriaTask.TerminationCriteriaResult;
import cloud.benchflow.experimentmanager.tasks.running.CheckTrialResultTask;
import cloud.benchflow.experimentmanager.tasks.running.CheckTrialResultTask.TrialExecutionStatus;
import cloud.benchflow.experimentmanager.tasks.running.DetermineAndExecuteTrialsTask;
import cloud.benchflow.experimentmanager.tasks.running.ReExecuteTrialTask;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-05
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public class RunningStatesHandler {

  /**
   * Every running state checks if the experiments has been terminated after each task to avoid that
   * the test state is overwritten.
   */

  private static Logger logger =
      LoggerFactory.getLogger(RunningStatesHandler.class.getSimpleName());

  private ConcurrentMap<String, AbortableFutureTask> experimentTasks;

  private BenchFlowExperimentModelDAO experimentModelDAO;
  private TrialModelDAO trialModelDAO;

  // TODO - should go into a stateless queue (so that we can recover)
  private CustomFutureReturningExecutor experimentTaskExecutorService;
  private ExperimentTaskScheduler experimentTaskScheduler;

  public RunningStatesHandler(ConcurrentMap<String, AbortableFutureTask> experimentTasks,
      ExperimentTaskScheduler experimentTaskScheduler,
      CustomFutureReturningExecutor experimentTaskExecutorService) {

    this.experimentTasks = experimentTasks;
    this.experimentTaskScheduler = experimentTaskScheduler;
    this.experimentTaskExecutorService = experimentTaskExecutorService;

  }

  public void initialize() {

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();

  }

  public void handleDetermineAndExecuteTrials(String experimentID) {

    logger.info("handleDetermineAndExecuteTrials: " + experimentID);

    DetermineAndExecuteTrialsTask newTrialTask = new DetermineAndExecuteTrialsTask(experimentID);

    AbortableFutureTask future =
        (AbortableFutureTask) experimentTaskExecutorService.submit(newTrialTask);

    // replace with new task
    experimentTasks.put(experimentID, future);

    try {

      // wait for task to complete
      AbortableFutureTaskResult futureResult =
          experimentTaskScheduler.getAbortableFutureTask(future);

      if (futureResult.isAborted()) {
        logger.info("Task has been aborted for experiment: " + experimentID);
        return;
      }

      experimentModelDAO.setRunningState(experimentID, RunningState.HANDLE_TRIAL_RESULT);

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

  }

  public void handleTrialResult(String experimentID) {

    logger.info("handleTrialResult: " + experimentID);

    try {

      String trialID = experimentModelDAO.getLastExecutedTrialID(experimentID);

      handleCheckTrialResultTask(trialID);

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      e.printStackTrace();
    }
  }

  private void handleCheckTrialResultTask(String trialID) {

    logger.info("handleCheckTrialResultTask: " + trialID);

    // update the trial status
    determineAndSetTrialStatus(trialID);

    CheckTrialResultTask trialResultTask = new CheckTrialResultTask(trialID);

    AbortableFutureTask<TrialExecutionStatus> future =
        (AbortableFutureTask<TrialExecutionStatus>) experimentTaskExecutorService
            .submit(trialResultTask);

    String experimentID = BenchFlowConstants.getExperimentIDFromTrialID(trialID);

    // replace with new task
    experimentTasks.put(experimentID, future);

    try {

      // wait for task to complete
      AbortableFutureTaskResult<TrialExecutionStatus> futureResult =
          experimentTaskScheduler.getAbortableFutureTask(future);

      if (futureResult.isAborted()) {
        logger.info("Task has been aborted for experiment: " + experimentID);
        return;
      }

      switch (futureResult.getResult()) {

        case SUCCESS:
          experimentModelDAO.setRunningState(experimentID,
              BenchFlowExperimentModel.RunningState.CHECK_TERMINATION_CRITERIA);

          break;

        case RE_EXECUTE_TRIAL:
          handleReExecuteTrial(trialID);
          break;

        default:
          // no default
          break;

      }

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

  }

  @VisibleForTesting
  public void handleReExecuteTrial(String trialID) {

    logger.info("handleReExecuteTrial: " + trialID);

    try {

      ReExecuteTrialTask reExecuteTrialTask = new ReExecuteTrialTask(trialID);

      AbortableFutureTask future =
          (AbortableFutureTask) experimentTaskExecutorService.submit(reExecuteTrialTask);

      String experimentID = BenchFlowConstants.getExperimentIDFromTrialID(trialID);

      // replace with new task
      experimentTasks.put(experimentID, future);

      // wait for task to complete
      AbortableFutureTaskResult futureResult =
          experimentTaskScheduler.getAbortableFutureTask(future);

      if (futureResult.isAborted()) {
        logger.info("Task has been aborted for experiment: " + experimentID);
        return;
      }

      experimentModelDAO.setRunningState(experimentID, RunningState.HANDLE_TRIAL_RESULT);

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private void determineAndSetTrialStatus(String trialID) {

    StatusCode statusCode = trialModelDAO.getFabanStatus(trialID);
    Result result = trialModelDAO.getFabanResult(trialID);

    if (statusCode == StatusCode.COMPLETED) {

      switch (result) {
        // if completed we check the result
        case PASSED:
          trialModelDAO.setTrialStatus(trialID, TrialStatus.SUCCESS);
          break;

        case FAILED:
          trialModelDAO.setTrialStatus(trialID, TrialStatus.FAILED);
          break;

        case NA:
        case UNKNOWN:
        case NOT_AVAILABLE:
        default:
          trialModelDAO.setTrialStatus(trialID, TrialStatus.RANDOM_FAILURE);
          break;
      }

    } else {
      // if not completed we treat it as a failed execution
      // could be FAILED, KILLED, KILLING OR DENIED
      trialModelDAO.setTrialStatus(trialID, TrialStatus.FAILED);
    }
  }

  public void handleCheckTerminationCriteria(String experimentID) {

    logger.info("handleCheckTerminationCriteria: " + experimentID);

    CheckTerminationCriteriaTask terminationCriteriaTask =
        new CheckTerminationCriteriaTask(experimentID);

    AbortableFutureTask<TerminationCriteriaResult> future =
        (AbortableFutureTask<TerminationCriteriaResult>) experimentTaskExecutorService
            .submit(terminationCriteriaTask);

    // replace with new task
    experimentTasks.put(experimentID, future);

    try {

      // wait for task to complete
      AbortableFutureTaskResult<TerminationCriteriaResult> futureResult =
          experimentTaskScheduler.getAbortableFutureTask(future);

      if (futureResult.isAborted()) {
        logger.info("Task has been aborted for experiment: " + experimentID);
        return;
      }

      switch (futureResult.getResult()) {
        case FULFILLED:
          experimentModelDAO.setExperimentState(experimentID, TERMINATED);
          experimentModelDAO.setTerminatedState(experimentID,
              BenchFlowExperimentModel.TerminatedState.COMPLETED);
          break;

        case NOT_FULFILLED:
          experimentModelDAO.setRunningState(experimentID, RunningState.DETERMINE_EXECUTE_TRIALS);
          break;

        case CANNOT_BE_FULFILLED:
          experimentModelDAO.setExperimentState(experimentID, TERMINATED);
          experimentModelDAO.setTerminatedState(experimentID,
              BenchFlowExperimentModel.TerminatedState.FAILURE);

          break;

        default:
          // no default
          break;
      }

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  public void handleTerminating(String experimentID) {

    logger.info("handleTerminating: " + experimentID);

    // set experiment to terminated
    experimentModelDAO.setExperimentState(experimentID, TERMINATED);
    experimentModelDAO.setTerminatedState(experimentID, TerminatedState.ABORTED);

  }

}
