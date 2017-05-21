package cloud.benchflow.experimentmanager.scheduler;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.configurations.factory.TestManagerServiceFactory;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-05-19
 */
public class ExperimentDispatcher implements Runnable {

  private static Logger logger =
      LoggerFactory.getLogger(ExperimentDispatcher.class.getSimpleName());

  private final BlockingQueue<String> readyQueue;
  private final BlockingQueue<String> runningQueue;

  private BenchFlowExperimentModelDAO experimentModelDAO;
  private BenchFlowTestManagerService testManagerService;
  private ExperimentTaskScheduler taskScheduler;

  public ExperimentDispatcher(BlockingQueue<String> readyQueue,
      BlockingQueue<String> runningQueue) {
    this.readyQueue = readyQueue;
    this.runningQueue = runningQueue;

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.testManagerService = BenchFlowExperimentManagerApplication.getTestManagerService();
  }

  @Override
  public void run() {

    logger.info("running");


    while (true) {

      try {

        // wait for next experiment to be ready
        String experimentID = readyQueue.take();

        logger.info("Scheduling experiment for execution: " + experimentID);

        // TODO - decide on String value that terminates the service

        // wait for running experiment to terminate and then schedule next
        runningQueue.put(experimentID);

        // change state to running and execute new trial
        experimentModelDAO.setExperimentState(experimentID,
            BenchFlowExperimentModel.BenchFlowExperimentState.RUNNING);
        experimentModelDAO.setRunningState(experimentID,
            BenchFlowExperimentModel.RunningState.EXECUTE_NEW_TRIAL);
        // inform test-manager that a new trial is being executed
        testManagerService.setExperimentRunningState(experimentID,
            BenchFlowExperimentModel.RunningState.EXECUTE_NEW_TRIAL);

        if (taskScheduler == null) {
          // can only be called once taskScheduler has been instantiated.
          this.taskScheduler = BenchFlowExperimentManagerApplication.getExperimentTaskScheduler();
        }

        // run next experiment
        taskScheduler.handleExperimentState(experimentID);

      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    }

  }

}
