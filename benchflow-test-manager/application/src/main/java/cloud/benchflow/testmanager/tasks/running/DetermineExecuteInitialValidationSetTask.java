package cloud.benchflow.testmanager.tasks.running;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-05-29
 */
public class DetermineExecuteInitialValidationSetTask implements Runnable {

  private static Logger logger =
      LoggerFactory.getLogger(DetermineExecuteInitialValidationSetTask.class.getSimpleName());

  private final String testID;

  public DetermineExecuteInitialValidationSetTask(String testID) {
    this.testID = testID;
  }

  @Override
  public void run() {
    logger.info("running: " + testID);
    // TODO
  }
}
