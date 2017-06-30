package cloud.benchflow.testmanager.services.internal.dao;

import cloud.benchflow.dsl.definition.configuration.goal.goaltype.GoalType;
import cloud.benchflow.dsl.definition.configuration.strategy.regression.RegressionStrategyType;
import cloud.benchflow.dsl.definition.configuration.strategy.selection.SelectionStrategyType;
import cloud.benchflow.dsl.definition.configuration.strategy.validation.ValidationStrategyType;
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.ExplorationSpace;
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.ExplorationSpaceDimensions;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.strategy.regression.MarsRegressionStrategy;
import cloud.benchflow.testmanager.strategy.regression.RegressionStrategy;
import cloud.benchflow.testmanager.strategy.selection.OneAtATimeSelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.RandomBreakdownSelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.SelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.SingleExperimentSelectionStrategy;
import cloud.benchflow.testmanager.strategy.validation.RandomValidationSetValidationStrategy;
import cloud.benchflow.testmanager.strategy.validation.ValidationStrategy;
import com.mongodb.MongoClient;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-25
 */
public class ExplorationModelDAO extends DAO {

  private static Logger logger = LoggerFactory.getLogger(ExplorationModelDAO.class.getSimpleName());

  private BenchFlowTestModelDAO testModelDAO;

  public ExplorationModelDAO(MongoClient mongoClient, BenchFlowTestModelDAO testModelDAO) {
    super(mongoClient);
    this.testModelDAO = testModelDAO;
  }

  public synchronized GoalType getGoalType(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("getGoalType: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    return benchFlowTestModel.getExplorationModel().getGoalType();

  }

  public synchronized void setGoalType(String testID, GoalType goalType)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("setGoalType: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    benchFlowTestModel.getExplorationModel().setGoalType(goalType);

    datastore.save(benchFlowTestModel);

  }

  public synchronized ExplorationSpaceDimensions getExplorationSpaceDimensions(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("getExplorationSpaceDimensions: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    return benchFlowTestModel.getExplorationModel().getExplorationSpaceDimensions();

  }

  public synchronized void setExplorationSpaceDimensions(String testID,
      ExplorationSpaceDimensions explorationSpaceDimensions)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("setExplorationSpaceDimensions: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    benchFlowTestModel.getExplorationModel()
        .setExplorationSpaceDimensions(explorationSpaceDimensions);

    datastore.save(benchFlowTestModel);

  }

  public synchronized ExplorationSpace getExplorationSpace(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("getExplorationSpace: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    return benchFlowTestModel.getExplorationModel().getExplorationSpace();

  }

  public synchronized void setExplorationSpace(String testID, ExplorationSpace explorationSpace)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("setExplorationSpace: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    benchFlowTestModel.getExplorationModel().setExplorationSpace(explorationSpace);

    datastore.save(benchFlowTestModel);

  }

  public synchronized List<Integer> getExecutedExplorationPointIndices(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("getExecutedExplorationPointIndices: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    return benchFlowTestModel.getExplorationModel().getExecutedExplorationPointIndices();
  }

  public synchronized void addExecutedExplorationPoint(String testID, int explorationPointIndex)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("addExecutedExplorationPoint: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    benchFlowTestModel.getExplorationModel().addExecutedExplorationPoint(explorationPointIndex);

    datastore.save(benchFlowTestModel);
  }

  public synchronized SelectionStrategy getSelectionStrategy(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("getSelectionStrategy: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    if (benchFlowTestModel.getExplorationModel().isSingleExperiment()) {
      return new SingleExperimentSelectionStrategy();
    }

    switch (benchFlowTestModel.getExplorationModel().getSelectionStrategyType()) {

      case ONE_AT_A_TIME:
        return new OneAtATimeSelectionStrategy();

      case RANDOM_BREAK_DOWN:
        return new RandomBreakdownSelectionStrategy();

      case BOUNDARY_FIRST:
      default:
        logger.info("not yet implemented");
        return null;
    }
  }

  public synchronized void setSelectionStrategyType(String testID,
      SelectionStrategyType strategyType) throws BenchFlowTestIDDoesNotExistException {

    logger.info("setSelectionStrategyType: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    benchFlowTestModel.getExplorationModel().setSelectionStrategyType(strategyType);

    datastore.save(benchFlowTestModel);
  }

  public synchronized ValidationStrategy getValidationStrategy(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("getValidationStrategy: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    switch (benchFlowTestModel.getExplorationModel().getValidationStrategyType()) {
      case RANDOM_VALIDATION_SET:
        return new RandomValidationSetValidationStrategy();
      default:
        logger.info("not yet implemented");
        return null;
    }
  }

  public synchronized void setValidationStrategyType(String testID,
      ValidationStrategyType strategyType) throws BenchFlowTestIDDoesNotExistException {

    logger.info("setValidationStrategyType: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    benchFlowTestModel.getExplorationModel().setValidationStrategyType(strategyType);

    datastore.save(benchFlowTestModel);
  }

  public synchronized RegressionStrategy getRegressionStrategy(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("getRegressionStrategy: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    switch (benchFlowTestModel.getExplorationModel().getRegressionStrategyType()) {
      case MARS:
        return new MarsRegressionStrategy();
      default:
        logger.info("not yet implemented");
        return null;
    }
  }

  public synchronized void setRegressionStrategyType(String testID,
      RegressionStrategyType strategyType) throws BenchFlowTestIDDoesNotExistException {

    logger.info("setRegressionStrategyType: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    benchFlowTestModel.getExplorationModel().setRegressionStrategyType(strategyType);

    datastore.save(benchFlowTestModel);
  }

  public synchronized boolean hasRegressionModel(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("hasRegressionModel: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    return benchFlowTestModel.getExplorationModel().hasRegressionModel();

  }

  public synchronized void setHasRegressionModel(String testID, boolean hasRegressionModel)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("setHasRegressionModel: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    benchFlowTestModel.getExplorationModel().setHasRegressionModel(hasRegressionModel);

    datastore.save(benchFlowTestModel);

  }

  public synchronized boolean isSingleExperiment(String testID)
      throws BenchFlowTestIDDoesNotExistException {

    logger.info("isSingleExperiment: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    return benchFlowTestModel.getExplorationModel().isSingleExperiment();

  }

  public synchronized void setSingleExperiment(String testID, boolean singleExperiment)
      throws BenchFlowTestIDDoesNotExistException {
    logger.info("setSingleExperiment: " + testID);

    final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

    benchFlowTestModel.getExplorationModel().setSingleExperiment(singleExperiment);

    datastore.save(benchFlowTestModel);
  }
}
