package cloud.benchflow.testmanager.strategy.validation;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-30
 */
public class SingleExperimentValidationStrategy implements ValidationStrategy {

  private static Logger logger =
      LoggerFactory.getLogger(SingleExperimentValidationStrategy.class.getSimpleName());

  private final ExplorationModelDAO explorationModelDAO;

  public SingleExperimentValidationStrategy() {
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  @Override
  public boolean isTestComplete(String testID) {

    logger.info("isTestComplete: " + testID);

    try {

      List<Integer> executedExplorationPointIndices =
          explorationModelDAO.getExecutedExplorationPointIndices(testID);
      return executedExplorationPointIndices.size() == 1;

    } catch (BenchFlowTestIDDoesNotExistException e) {
      e.printStackTrace();
    }

    return false;
  }
}
