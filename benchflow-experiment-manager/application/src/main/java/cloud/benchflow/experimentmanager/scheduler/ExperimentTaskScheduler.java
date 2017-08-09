package cloud.benchflow.experimentmanager.scheduler;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.experimentmanager.models.TrialModel.TrialStatus;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.tasks.running.CheckTerminationCriteriaTask;
import cloud.benchflow.experimentmanager.tasks.running.CheckTerminationCriteriaTask.TerminationCriteriaResult;
import cloud.benchflow.experimentmanager.tasks.running.CheckTrialResultTask;
import cloud.benchflow.experimentmanager.tasks.running.CheckTrialResultTask.TrialExecutionStatus;
import cloud.benchflow.experimentmanager.tasks.running.DetermineAndExecuteTrialsTask;
import cloud.benchflow.experimentmanager.tasks.running.ReExecuteTrialTask;
import cloud.benchflow.experimentmanager.tasks.running.execute.ExecuteTrial.FabanStatus;
import cloud.benchflow.experimentmanager.tasks.start.StartTask;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
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
  private TrialModelDAO trialModelDAO;
  // TODO - should go into a stateless queue (so that we can recover)
  private ExecutorService experimentTaskExecutorService;
  private BenchFlowTestManagerService testManagerService;

  public ExperimentTaskScheduler(ExecutorService experimentTaskExecutorService) {
    this.experimentTaskExecutorService = experimentTaskExecutorService;
  }

  public void initialize() {

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
    this.testManagerService = BenchFlowExperimentManagerApplication.getTestManagerService();

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
          // remove test from running queue so dispatcher can run next experiment
          runningQueue.remove(experimentID);
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

  private void handleStartState(String experimentID) {

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

      switch (runningState) {
        case DETERMINE_EXECUTE_TRIALS:
          handleDetermineAndExecuteTrials(experimentID);
          break;

        case HANDLE_TRIAL_RESULT:
          handleTrialResult(experimentID);
          break;

        case CHECK_TERMINATION_CRITERIA:
          handleCheckTerminationCriteria(experimentID);
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

  private void handleDetermineAndExecuteTrials(String experimentID) {

    logger.info("handleDetermineAndExecuteTrials: " + experimentID);

    DetermineAndExecuteTrialsTask newTrialTask = new DetermineAndExecuteTrialsTask(experimentID);

    Future<FabanStatus> future = experimentTaskExecutorService.submit(newTrialTask);

    experimentTasks.put(experimentID, future);

    // TODO - change this when faban interaction changes to non-polling

    try {

      FabanStatus fabanStatus = future.get();

      String trialID = fabanStatus.getTrialID();

      trialModelDAO.setFabanStatus(trialID, fabanStatus.getStatusCode());
      trialModelDAO.setFabanResult(trialID, fabanStatus.getResult());

      determineAndSetTrialStatus(trialID, fabanStatus);

      experimentModelDAO.setRunningState(experimentID, RunningState.HANDLE_TRIAL_RESULT);
      testManagerService.setExperimentRunningState(experimentID, RunningState.HANDLE_TRIAL_RESULT);

      handleExperimentState(experimentID);

    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

  }

  private void handleTrialResult(String experimentID) {

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

      switch (experimentResult) {

        case SUCCESS:
          experimentModelDAO.setRunningState(experimentID,
              BenchFlowExperimentModel.RunningState.CHECK_TERMINATION_CRITERIA);
          testManagerService.setExperimentRunningState(experimentID,
              RunningState.CHECK_TERMINATION_CRITERIA);

          handleExperimentState(experimentID);
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

        case NOT_FULFILLED:
          experimentModelDAO.setRunningState(experimentID, RunningState.DETERMINE_EXECUTE_TRIALS);
          testManagerService.setExperimentRunningState(experimentID,
              RunningState.DETERMINE_EXECUTE_TRIALS);
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
