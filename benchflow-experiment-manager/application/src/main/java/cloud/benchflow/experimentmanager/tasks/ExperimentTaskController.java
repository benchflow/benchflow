package cloud.benchflow.experimentmanager.tasks;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.tasks.running.ExperimentRunningTask;
import cloud.benchflow.experimentmanager.tasks.start.StartTask;
import cloud.benchflow.faban.client.FabanClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class ExperimentTaskController {

  private static Logger logger =
      LoggerFactory.getLogger(ExperimentTaskController.class.getSimpleName());

  private ConcurrentMap<String, Future> experimentTasks = new ConcurrentHashMap<>();

  // TODO - running queue (1 element)

  // TODO - ready queue

  private BenchFlowExperimentModelDAO experimentModelDAO;
  private ExecutorService experimentTaskExecutorService;
  private int submitRetries;

  public ExperimentTaskController(
      ExecutorService experimentTaskExecutorService, int submitRetries) {

    this.experimentTaskExecutorService = experimentTaskExecutorService;
    this.submitRetries = submitRetries;

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
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
          setNextExperimentState(experimentID, BenchFlowExperimentState.RUNNING);
          // TODO - put test in shared (with dispatcher) ready queue
          break;

        case RUNNING:
          handleRunState(experimentID);
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

  private void setNextExperimentState(
      String experimentID, BenchFlowExperimentState experimentState) {

    // change state to experimentState
    experimentModelDAO.setExperimentState(experimentID, experimentState);

    handleExperimentState(experimentID);
  }

  private synchronized void handleStartState(String experimentID) {

    logger.info("handleStartState: " + experimentID);

    StartTask startTask = new StartTask(experimentID);

    // TODO - should go into a stateless queue (so that we can recover)
    Future<?> future = experimentTaskExecutorService.submit(startTask);

    experimentTasks.put(experimentID, future);

    try {

      future.get();
      experimentModelDAO.setExperimentState(experimentID, BenchFlowExperimentState.READY);

      handleExperimentState(experimentID);

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle properly
      e.printStackTrace();
    }
  }

  private synchronized void handleRunState(String experimentID) {

    logger.info("handleRunState: " + experimentID);

    ExperimentRunningTask runningTask = new ExperimentRunningTask(experimentID, submitRetries);

    Future<?> future = experimentTaskExecutorService.submit(runningTask);

    // replace previous task
    experimentTasks.put(experimentID, future);

    try {

      // TODO - should return result and control flow should be handled here
      future.get();

    } catch (InterruptedException | ExecutionException e) {
      // TODO - handle properly
      e.printStackTrace();
    }
  }
}
