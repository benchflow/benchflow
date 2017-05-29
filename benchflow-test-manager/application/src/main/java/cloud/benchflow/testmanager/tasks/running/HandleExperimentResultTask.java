package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05
 */
public class HandleExperimentResultTask implements Callable<Boolean> {

  private static Logger logger =
      LoggerFactory.getLogger(HandleExperimentResultTask.class.getSimpleName());
  private final String testID;

  private ExplorationModelDAO explorationModelDAO;

  public HandleExperimentResultTask(String testID) {

    this.testID = testID;

    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  @Override
  public Boolean call() throws Exception {

    logger.info("running: " + testID);

    // TODO - get results when needed

    return explorationModelDAO.hasRegressionModel(testID);

  }

}
