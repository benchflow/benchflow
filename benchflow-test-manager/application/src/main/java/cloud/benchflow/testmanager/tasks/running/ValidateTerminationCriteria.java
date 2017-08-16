package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.validation.CompleteExplorationValidationStrategy;
import cloud.benchflow.testmanager.strategy.validation.SingleExperimentValidationStrategy;
import cloud.benchflow.testmanager.strategy.validation.ValidationStrategy;
import cloud.benchflow.testmanager.tasks.AbortableCallable;
import cloud.benchflow.testmanager.tasks.running.ValidateTerminationCriteria.TerminationCriteriaResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-05
 */
public class ValidateTerminationCriteria extends AbortableCallable<TerminationCriteriaResult> {

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

    // TODO - check if can reach goal
    //    boolean canReachGoal = true;

    // has regression model
    //    boolean hasRegressionModel = explorationModelDAO.hasRegressionModel(testID);

    // TODO - handle cases with regression model

    // cases with no regression model, e.g. complete exploration or single experiment
    ValidationStrategy validationStrategy;
    boolean singleExperiment = explorationModelDAO.isSingleExperiment(testID);
    if (singleExperiment) {
      validationStrategy = new SingleExperimentValidationStrategy();
    } else {
      validationStrategy = new CompleteExplorationValidationStrategy();
    }

    if (validationStrategy.isTestComplete(testID)) {
      return TerminationCriteriaResult.GOAL_REACHABLE_NO_REGRESSION_EXPERIMENTS_EXECUTED;
    }

    return TerminationCriteriaResult.GOAL_REACHABLE_NO_REGRESSION_EXPERIMENTS_REMAINING;

  }

  public enum TerminationCriteriaResult {
    GOAL_NOT_REACHABLE, GOAL_REACHABLE_NO_REGRESSION_EXPERIMENTS_REMAINING, GOAL_REACHABLE_NO_REGRESSION_EXPERIMENTS_EXECUTED, GOAL_REACHABLE_REGRESSION_PREDICTION_NOT_ACCEPTABLE, GOAL_REACHABLE_REGRESSION_PREDICTION_ACCEPTABLE
  }
}
