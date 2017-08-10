package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.tasks.AbortableRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05
 */
public class AddStoredKnowledgeTask extends AbortableRunnable {

  private static Logger logger =
      LoggerFactory.getLogger(AddStoredKnowledgeTask.class.getSimpleName());

  private final String testID;

  public AddStoredKnowledgeTask(String testID) {
    this.testID = testID;

  }

  @Override
  public void run() {
    logger.info("running: " + testID);

    // TODO - add stored knowledge
  }


}
