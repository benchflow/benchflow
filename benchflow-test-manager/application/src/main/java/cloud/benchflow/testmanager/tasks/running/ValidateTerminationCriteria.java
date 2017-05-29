package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.OneAtATimeSelectionStrategy;
import cloud.benchflow.testmanager.tasks.running.ValidateTerminationCriteria.TerminationCriteriaResult;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05
 */
public class ValidateTerminationCriteria implements Callable<TerminationCriteriaResult> {

  private static Logger logger =
      LoggerFactory.getLogger(HandleExperimentResultTask.class.getSimpleName());

  private final String testID;

  private final ExplorationModelDAO explorationModelDAO;

  public ValidateTerminationCriteria(String testID) {
    this.testID = testID;
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  @Override
  public TerminationCriteriaResult call() throws Exception {

    logger.info("running: " + testID);

    OneAtATimeSelectionStrategy oneAtATimeSelectionStrategy =
        (OneAtATimeSelectionStrategy) explorationModelDAO.getSelectionStrategy(testID);

    // TODO - check if can reach goal
    boolean canReachGoal = true;

    // has regression model
    boolean hasRegressionModel = explorationModelDAO.hasRegressionModel(testID);

    // TODO - handle cases with regression model


    if (oneAtATimeSelectionStrategy.isTestComplete(testID)) {
      return TerminationCriteriaResult.GOAL_NO_REGRESSION_EXECUTED;
    }

    return TerminationCriteriaResult.GOAL_NO_REGRESSION_REMAINING;

  }

  public enum TerminationCriteriaResult {
    CANNOT_REACH_GOAL, GOAL_NO_REGRESSION_REMAINING, GOAL_NO_REGRESSION_EXECUTED, GOAL_REGRESSION_NOT_ACCEPTABLE, GOAL_REGRESSION_ACCEPTABLE
  }
}
