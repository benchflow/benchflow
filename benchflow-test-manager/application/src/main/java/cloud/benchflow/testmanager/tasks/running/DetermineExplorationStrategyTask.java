package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.tasks.AbortableRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-05-05
 */
public class DetermineExplorationStrategyTask extends AbortableRunnable {

  private static Logger logger =
      LoggerFactory.getLogger(DetermineExplorationStrategyTask.class.getSimpleName());

  private final String testID;

  public DetermineExplorationStrategyTask(String testID) {
    this.testID = testID;
  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    // TODO - set default exploration strategy if not defined by user

  }
}
