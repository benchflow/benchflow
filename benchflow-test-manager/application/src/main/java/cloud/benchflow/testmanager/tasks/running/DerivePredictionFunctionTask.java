package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.tasks.AbortableRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05
 */
public class DerivePredictionFunctionTask extends AbortableRunnable {

  private static Logger logger =
      LoggerFactory.getLogger(DerivePredictionFunctionTask.class.getSimpleName());

  private final String testID;

  public DerivePredictionFunctionTask(String testID) {
    this.testID = testID;
  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    // TODO - derive prediction function

  }
}
