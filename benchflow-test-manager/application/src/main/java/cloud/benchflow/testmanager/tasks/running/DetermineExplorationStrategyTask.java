package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05
 */
public class DetermineExplorationStrategyTask implements Runnable {

  private static Logger logger =
      LoggerFactory.getLogger(DetermineExplorationStrategyTask.class.getSimpleName());

  private final String testID;

  // services
  private final ExplorationModelDAO explorationModelDAO;

  public DetermineExplorationStrategyTask(String testID) {
    this.testID = testID;
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  @Override
  public void run() {

    // TODO - read this from BenchFlowTest

    ExperimentSelectionStrategy.Type selectionStrategyType =
        ExperimentSelectionStrategy.Type.COMPLETE_SELECTION;

    try {

      explorationModelDAO.setExperimentSelectionStrategy(testID, selectionStrategyType);

      // TODO - change this depending on real value
      explorationModelDAO.setHasRegressionModel(testID, false);

    } catch (BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it has already been added
      logger.error("should not happen");
      e.printStackTrace();
    }
  }
}
