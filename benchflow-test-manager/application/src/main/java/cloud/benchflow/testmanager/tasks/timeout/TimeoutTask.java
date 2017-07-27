package cloud.benchflow.testmanager.tasks.timeout;

import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-02
 */
public class TimeoutTask implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(TimeoutTask.class.getSimpleName());

  private String testID;
  private TestTaskScheduler testTaskScheduler;

  public TimeoutTask(String testID, TestTaskScheduler testTaskScheduler) {
    this.testID = testID;
    this.testTaskScheduler = testTaskScheduler;
  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    testTaskScheduler.terminateTest(testID);

  }
}
