package cloud.benchflow.experimentmanager.scheduler.running;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.experimentmanager.models.TrialModel.TrialStatus;
import cloud.benchflow.experimentmanager.scheduler.ExperimentTaskScheduler;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.running.CheckTerminationCriteriaTask;
import cloud.benchflow.experimentmanager.tasks.running.CheckTerminationCriteriaTask.TerminationCriteriaResult;
import cloud.benchflow.experimentmanager.tasks.running.CheckTrialResultTask;
import cloud.benchflow.experimentmanager.tasks.running.CheckTrialResultTask.TrialExecutionStatus;
import cloud.benchflow.experimentmanager.tasks.running.DetermineAndExecuteTrialsTask;
import cloud.benchflow.experimentmanager.tasks.running.ReExecuteTrialTask;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial.FabanStatus;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-05
 */
public class RunningStatesHandler {

  /**
   * Every running state checks if the experiments has been terminated after each task
   * to avoid that the test state is overwritten.
   */

  private static Logger logger =
      LoggerFactory.getLogger(RunningStatesHandler.class.getSimpleName());

  private ConcurrentMap<String, Future> experimentTasks = new ConcurrentHashMap<>();

  private BenchFlowExperimentModelDAO experimentModelDAO;
  private TrialModelDAO trialModelDAO;

  // TODO - should go into a stateless queue (so that we can recover)
  private ExecutorService experimentTaskExecutorService;
  private ExperimentTaskScheduler experimentTaskScheduler;

  public RunningStatesHandler(ConcurrentMap<String, Future> experimentTasks,
      ExperimentTaskScheduler experimentTaskScheduler,
      ExecutorService experimentTaskExecutorService) {

    this.experimentTasks = experimentTasks;
    this.experimentTaskScheduler = experimentTaskScheduler;
    this.experimentTaskExecutorService = experimentTaskExecutorService;

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();

  }

  public void handleDetermineAndExecuteTrials(String experimentID) {

    logger.info("handleDetermineAndExecuteTrials: " + experimentID);

    DetermineAndExecuteTrialsTask newTrialTask = new DetermineAndExecuteTrialsTask(experimentID);

    Future<FabanStatus> future = experimentTaskExecutorService.submit(newTrialTask);

    experimentTasks.put(experimentID, future);

    // TODO - change this when faban interaction changes to non-polling

    try {

      FabanStatus fabanStatus = future.get();

      if (experimentTaskScheduler.isTerminated(experimentID)) {
        // if test has been terminated we stop here
        return;
      }

      String trialID = fabanStatus.getTrialID();

      trialModelDAO.setFabanStatus(trialID, fabanStatus.getStatusCode());
      trialModelDAO.setFabanResult(trialID, fabanStatus.getResult());

      determineAndSetTrialStatus(trialID, fabanStatus);

      experimentModelDAO.setRunningState(experimentID, RunningState.HANDLE_TRIAL_RESULT);

      experimentTaskScheduler.handleExperimentState(experimentID);

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

    CheckTrialResultTask trialResultTask = new CheckTrialResultTask(trialID);

    Future<TrialExecutionStatus> future = experimentTaskExecutorService.submit(trialResultTask);

    String experimentID = BenchFlowConstants.getExperimentIDFromTrialID(trialID);
    experimentTasks.put(experimentID, future);

    try {

      TrialExecutionStatus experimentResult = future.get();

      if (experimentTaskScheduler.isTerminated(experimentID)) {
        // if test has been terminated we stop here
        return;
      }

      switch (experimentResult) {

        case SUCCESS:
          experimentModelDAO.setRunningState(experimentID,
              BenchFlowExperimentModel.RunningState.CHECK_TERMINATION_CRITERIA);

          experimentTaskScheduler.handleExperimentState(experimentID);
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

  private void handleReExecuteTrial(String trialID) {

    logger.info("handleReExecuteTrial: " + trialID);

    try {

      ReExecuteTrialTask reExecuteTrialTask = new ReExecuteTrialTask(trialID);

      Future<FabanStatus> future = experimentTaskExecutorService.submit(reExecuteTrialTask);

      String experimentID = BenchFlowConstants.getExperimentIDFromTrialID(trialID);
      experimentTasks.put(experimentID, future);

      // TODO - change this when faban interaction changes to non-polling
      FabanStatus fabanStatus = future.get();

      if (experimentTaskScheduler.isTerminated(experimentID)) {
        // if test has been terminated we stop here
        return;
      }

      trialModelDAO.setFabanStatus(trialID, fabanStatus.getStatusCode());
      trialModelDAO.setFabanResult(trialID, fabanStatus.getResult());

      determineAndSetTrialStatus(trialID, fabanStatus);

      handleCheckTrialResultTask(trialID);

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private void determineAndSetTrialStatus(String trialID, FabanStatus fabanStatus) {
    if (fabanStatus.getStatusCode() == StatusCode.COMPLETED) {

      switch (fabanStatus.getResult()) {
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

    Future<TerminationCriteriaResult> future =
        experimentTaskExecutorService.submit(terminationCriteriaTask);

    experimentTasks.put(experimentID, future);

    try {

      TerminationCriteriaResult terminationCriteriaResult = future.get();

      if (experimentTaskScheduler.isTerminated(experimentID)) {
        // if test has been terminated we stop here
        return;
      }

      switch (terminationCriteriaResult) {
        case FULFILLED:
          experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
          experimentModelDAO.setTerminatedState(experimentID,
              BenchFlowExperimentModel.TerminatedState.COMPLETED);

          experimentTaskScheduler.handleExperimentState(experimentID);

          break;

        case NOT_FULFILLED:
          experimentModelDAO.setRunningState(experimentID, RunningState.DETERMINE_EXECUTE_TRIALS);
          experimentTaskScheduler.handleExperimentState(experimentID);

          break;

        case CANNOT_BE_FULFILLED:
          experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.TERMINATED);
          experimentModelDAO.setTerminatedState(experimentID,
              BenchFlowExperimentModel.TerminatedState.FAILURE);

          experimentTaskScheduler.handleExperimentState(experimentID);

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
