package cloud.benchflow.testmanager.tasks.start;

import static cloud.benchflow.testmanager.strategy.selection.SelectionStrategy.Type.BOUNDARY_FIRST;
import static cloud.benchflow.testmanager.strategy.selection.SelectionStrategy.Type.ONE_AT_A_TIME;
import static cloud.benchflow.testmanager.strategy.selection.SelectionStrategy.Type.RANDOM_BREAKDOWN;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.dsl.definition.configuration.goal.goaltype.GoalType;
import cloud.benchflow.dsl.definition.configuration.strategy.selection.SelectionStrategyType;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.ExplorationModel;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.regression.RegressionStrategy;
import cloud.benchflow.testmanager.strategy.selection.SelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.SelectionStrategy.Type;
import cloud.benchflow.testmanager.strategy.validation.ValidationStrategy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Enumeration.Value;
import scala.Option;

/**
 * Prepares the test for running.
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public class StartTask implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(StartTask.class.getSimpleName());

  private final String testID;

  // services
  private final MinioService minioService;
  private final ExplorationModelDAO explorationModelDAO;

  public StartTask(String testID) {

    this.testID = testID;

    this.minioService = BenchFlowTestManagerApplication.getMinioService();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();

  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    try {

      String testDefinitionYamlString =
          IOUtils.toString(minioService.getTestDefinition(testID), StandardCharsets.UTF_8);

      BenchFlowTest test = BenchFlowDSL.testFromYaml(testDefinitionYamlString);

      // save goal type
      Value goalTypeValue = test.configuration().goal().goalType();
      ExplorationModel.GoalType goalType = convertGoalTypeToJavaType(goalTypeValue);
      explorationModelDAO.setGoalType(testID, goalType);

      if (test.configuration().strategy().isDefined()) {

        // TODO - in future PR
        //        // get and save exploration space
        //        JavaCompatExplorationSpace explorationSpace =
        //            ExplorationSpace.explorationSpaceFromTestYaml(testDefinitionYamlString);
        //
        //        explorationModelDAO.setExplorationSpace(testID, explorationSpace);
        //
        //        // get and save exploration space dimensions
        //        JavaCompatExplorationSpaceDimensions explorationSpaceDimensions =
        //            cloud.benchflow.dsl.ExplorationSpace
        //                .explorationSpaceDimensionsFromTestYaml(testDefinitionYamlString);
        //
        //        explorationModelDAO.setExplorationSpaceDimensions(testID, explorationSpaceDimensions);

        // get and save selection strategy
        Value selectionStrategyTypeValue = test.configuration().strategy().get().selection();
        Type selectionStrategyType = convertSelectionStrategyToJavaType(selectionStrategyTypeValue);

        explorationModelDAO.setSelectionStrategyType(testID, selectionStrategyType);

        // get and save validation strategy
        Option<Value> validationStrategyOption = test.configuration().strategy().get().validation();

        if (validationStrategyOption.isDefined()) {
          Value validationStrategyValue = validationStrategyOption.get();
          ValidationStrategy.Type validationStrategyType =
              convertValidationStrategyToJavaType(validationStrategyValue);
          explorationModelDAO.setValidationStrategyType(testID, validationStrategyType);
        }

        // get and save regression strategy
        Option<Value> regressionStrategyOption = test.configuration().strategy().get().regression();
        explorationModelDAO.setHasRegressionModel(testID, regressionStrategyOption.isDefined());

        if (regressionStrategyOption.isDefined()) {
          Value regressionStrategyTypeValue = validationStrategyOption.get();
          RegressionStrategy.Type regressionStrategyType =
              convertRegressionStrategyToJavaType(regressionStrategyTypeValue);
          explorationModelDAO.setRegressionStrategyType(testID, regressionStrategyType);
        }

      }

    } catch (BenchFlowDeserializationException | BenchFlowTestIDDoesNotExistException
        | IOException e) {
      // should not happen since it has already been tested/added
      logger.error("should not happen");
      e.printStackTrace();
    }

    logger.info("completed: " + testID);

  }

  private SelectionStrategy.Type convertSelectionStrategyToJavaType(
      Value selectionStrategyTypeValue) {

    if (selectionStrategyTypeValue.equals(SelectionStrategyType.OneAtATime())) {
      return ONE_AT_A_TIME;
    }

    if (selectionStrategyTypeValue.equals(SelectionStrategyType.RandomBreakDown())) {
      return RANDOM_BREAKDOWN;
    }

    return BOUNDARY_FIRST;

  }

  private ExplorationModel.GoalType convertGoalTypeToJavaType(Value goalTypeValue) {

    if (goalTypeValue.equals(GoalType.Load())) {
      return ExplorationModel.GoalType.LOAD;
    }

    if (goalTypeValue.equals(GoalType.Configuration())) {
      return ExplorationModel.GoalType.CONFIGURATION;
    }

    return ExplorationModel.GoalType.EXPLORATION;

  }

  private ValidationStrategy.Type convertValidationStrategyToJavaType(
      Value validationStrategyTypeValue) {

    // to be changed when more strategies are added

    return ValidationStrategy.Type.RANDOM_VALIDATION_SET;

  }

  private RegressionStrategy.Type convertRegressionStrategyToJavaType(
      Value regressionStrategyTypeValue) {

    // to be changed when more strategies are added

    return RegressionStrategy.Type.MARS;

  }
}
