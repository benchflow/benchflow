package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05
 */
public class AddStoredKnowledgeTask implements Callable<Boolean> {

  private static Logger logger =
      LoggerFactory.getLogger(AddStoredKnowledgeTask.class.getSimpleName());

  private final String testID;

  private final ExplorationModelDAO explorationModelDAO;

  public AddStoredKnowledgeTask(String testID) {
    this.testID = testID;

    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  @Override
  public Boolean call() throws Exception {

    logger.info("running: " + testID);

    // TODO - add stored knowledge

    return explorationModelDAO.hasRegressionModel(testID);

  }


}
