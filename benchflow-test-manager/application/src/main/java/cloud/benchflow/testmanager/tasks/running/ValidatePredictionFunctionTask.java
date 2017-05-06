package cloud.benchflow.testmanager.tasks.running;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05 */
public class ValidatePredictionFunctionTask implements Callable<Boolean> {

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
