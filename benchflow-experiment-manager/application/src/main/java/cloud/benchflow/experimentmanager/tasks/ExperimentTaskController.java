package cloud.benchflow.experimentmanager.tasks;

import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.tasks.experiment.ExperimentReadyTask;
import cloud.benchflow.experimentmanager.tasks.experiment.ExperimentRunningTask;
import cloud.benchflow.faban.client.FabanClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class ExperimentTaskController {

  private static Logger logger =
      LoggerFactory.getLogger(ExperimentTaskController.class.getSimpleName());
  private ConcurrentMap<String, CancellableTask> experimentTasks = new ConcurrentHashMap<>();

  private MinioService minio;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private FabanClient faban;
  private DriversMakerService driversMaker;
  private BenchFlowTestManagerService testManagerService;
  private ExecutorService experimentTaskExecutorService;
  private int submitRetries;

  public ExperimentTaskController(
      MinioService minio,
      BenchFlowExperimentModelDAO experimentModelDAO,
      FabanClient faban,
      DriversMakerService driversMaker,
      BenchFlowTestManagerService testManagerService,
      ExecutorService experimentTaskExecutorService,
      int submitRetries) {
    this.minio = minio;
    this.experimentModelDAO = experimentModelDAO;
    this.faban = faban;
    this.driversMaker = driversMaker;
    this.testManagerService = testManagerService;
    this.experimentTaskExecutorService = experimentTaskExecutorService;
    this.submitRetries = submitRetries;
  }

  public synchronized void submitExperiment(String experimentID) {

    logger.info("submitExperiment: " + experimentID);

    if (experimentTasks.containsKey(experimentID)) {
      // TODO - throw exception?
      logger.info("submitExperiment: experiment already submitted");

      return;
    }

    ExperimentReadyTask readyTask =
        new ExperimentReadyTask(
            experimentID, this, experimentModelDAO, minio, faban, driversMaker, testManagerService);

    // TODO - should go into a stateless queue (so that we can recover)
    // (for now) only allows one experiment at a time (poolSize == 1)
    experimentTaskExecutorService.submit(readyTask);

    experimentTasks.put(experimentID, readyTask);
  }

  public synchronized void runExperiment(String experimentID) {

    logger.info("runExperiment: " + experimentID);

    if (!experimentTasks.containsKey(experimentID)) {
      // TODO - throw exception?
      logger.info("runExperiment: experiment has to be submitted first");

      return;
    }

    ExperimentRunningTask runningTask =
        new ExperimentRunningTask(
            experimentID, testManagerService, minio, faban, experimentModelDAO, submitRetries);

    experimentTaskExecutorService.submit(runningTask);

    // replace previous task
    experimentTasks.put(experimentID, runningTask);
  }
}
