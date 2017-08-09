package cloud.benchflow.testmanager.tasks.start;

import cloud.benchflow.dsl.BenchFlowTestAPI;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.dsl.definition.configuration.goal.goaltype.GoalType;
import cloud.benchflow.dsl.definition.configuration.strategy.regression.RegressionStrategyType;
import cloud.benchflow.dsl.definition.configuration.strategy.selection.SelectionStrategyType;
import cloud.benchflow.dsl.definition.configuration.strategy.validation.ValidationStrategyType;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.definition.types.time.Time;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.tasks.AbortableRunnable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

/**
 * Prepares the test for running.
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public class StartTask extends AbortableRunnable {

  private static Logger logger = LoggerFactory.getLogger(StartTask.class.getSimpleName());

  private final String testID;

  // services
  private final MinioService minioService;
  private final ExplorationModelDAO explorationModelDAO;
  private final BenchFlowTestModelDAO testModelDAO;

  public StartTask(String testID) {

    this.testID = testID;

    this.minioService = BenchFlowTestManagerApplication.getMinioService();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    try {

      String testDefinitionYamlString =
          IOUtils.toString(minioService.getTestDefinition(testID), StandardCharsets.UTF_8);

      BenchFlowTest test = BenchFlowTestAPI.testFromYaml(testDefinitionYamlString);

      // save goal type
      GoalType goalType = test.configuration().goal().goalType();
      explorationModelDAO.setGoalType(testID, goalType);

      if (goalType == GoalType.LOAD) {
        explorationModelDAO.setSingleExperiment(testID, true);
      }

      // save max run time
      Time maxRunTimeTime = test.configuration().terminationCriteria().test().maxTime();
      testModelDAO.setMaxRunTime(testID, maxRunTimeTime);

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
        if (test.configuration().strategy().isDefined()) {

          SelectionStrategyType selectionStrategyType =
              test.configuration().strategy().get().selection();

          explorationModelDAO.setSelectionStrategyType(testID, selectionStrategyType);

          // get and save validation strategy
          Option<ValidationStrategyType> validationStrategyOption =
              test.configuration().strategy().get().validation();

          if (validationStrategyOption.isDefined()) {
            ValidationStrategyType validationStrategyType = validationStrategyOption.get();
            explorationModelDAO.setValidationStrategyType(testID, validationStrategyType);
          }

          // get and save regression strategy
          Option<RegressionStrategyType> regressionStrategyOption =
              test.configuration().strategy().get().regression();
          // set has regression model
          explorationModelDAO.setHasRegressionModel(testID, regressionStrategyOption.isDefined());

          if (regressionStrategyOption.isDefined()) {
            RegressionStrategyType regressionStrategyType = regressionStrategyOption.get();
            explorationModelDAO.setRegressionStrategyType(testID, regressionStrategyType);
          }
        } else {
          // set has regression model to false since no strategy defined
          explorationModelDAO.setHasRegressionModel(testID, false);
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

}
