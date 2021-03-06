package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.tasks.AbortableCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05
 */
public class ValidatePredictionFunctionTask extends AbortableCallable<Boolean> {

  private static Logger logger =
      LoggerFactory.getLogger(ValidatePredictionFunctionTask.class.getSimpleName());

  private final String testID;

  public ValidatePredictionFunctionTask(String testID) {
    this.testID = testID;
  }

  @Override
  public Boolean call() throws Exception {

    logger.info("running: " + testID);

    // TODO - validate prediction function

    return true;
  }
}
