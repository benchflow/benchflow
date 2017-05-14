package cloud.benchflow.testmanager.tasks.running;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05
 */
public class RemoveNonReachableExperimentsTask implements Runnable {

  private static Logger logger =
      LoggerFactory.getLogger(RemoveNonReachableExperimentsTask.class.getSimpleName());

  private final String testID;

  public RemoveNonReachableExperimentsTask(String testID) {
    this.testID = testID;
  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    // TODO - remove non-reachable experiments

  }
}
