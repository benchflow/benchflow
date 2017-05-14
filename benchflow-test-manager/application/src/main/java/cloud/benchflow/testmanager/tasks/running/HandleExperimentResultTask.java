package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05 */
public class HandleExperimentResultTask implements Callable<HandleExperimentResultTask.Result> {

  private static Logger logger =
      LoggerFactory.getLogger(HandleExperimentResultTask.class.getSimpleName());
  private final String testID;

  private ExplorationModelDAO explorationModelDAO;

  public HandleExperimentResultTask(String testID) {

    this.testID = testID;

    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  @Override
  public Result call() throws Exception {

    logger.info("running: " + testID);

    ExperimentSelectionStrategy.Type selectionType =
        explorationModelDAO.getExperimentSelectionStrategyType(testID);

    switch (selectionType) {
      case COMPLETE_SELECTION:
        return Result.COMPLETE_SELECTION;

      // TODO - other cases:
      default:
        return Result.CAN_REACH_GOAL;
    }
  }

  public enum Result {
    COMPLETE_SELECTION, CAN_REACH_GOAL, CANNOT_REACH_GOAL
  }
}
