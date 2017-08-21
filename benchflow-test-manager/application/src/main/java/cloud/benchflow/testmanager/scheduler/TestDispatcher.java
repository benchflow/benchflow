package cloud.benchflow.testmanager.scheduler;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-05-28
 */
public class TestDispatcher implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(TestDispatcher.class.getSimpleName());

  private final BlockingQueue<String> readyQueue;
  private final BlockingQueue<String> runningQueue;

  private TestTaskScheduler taskScheduler;
  private final BenchFlowTestModelDAO testModelDAO;

  public TestDispatcher(BlockingQueue<String> readyQueue, BlockingQueue<String> runningQueue) {
    this.readyQueue = readyQueue;
    this.runningQueue = runningQueue;

    this.taskScheduler = BenchFlowTestManagerApplication.getTestTaskScheduler();
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
  }

  @VisibleForTesting
  public void setTaskScheduler(TestTaskScheduler taskScheduler) {
    this.taskScheduler = taskScheduler;
  }

  @Override
  public void run() {


    logger.info("running");

    while (true) {

      // wait for next test to be ready
      try {

        String testID = readyQueue.take();

        logger.info("Scheduling test for execution: " + testID);

        // TODO - decide on String value that terminates the service

        // wait for running test to terminate and then schedule next
        // the runningQueue accepts only one element and put is blocking
        runningQueue.put(testID);

        // change state to running and proceed to next state
        testModelDAO.setTestState(testID, BenchFlowTestState.RUNNING);

        // this is blocking, since we first complete the lifecycle of the test
        // we place in the runningQueue, then we return from the method
        taskScheduler.handleRunningTest(testID);

      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (BenchFlowTestIDDoesNotExistException e) {
        // should not happen at this stage
        e.printStackTrace();
      }

    }

  }
}
