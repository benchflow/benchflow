package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20 */
public class CompleteSelectionStrategy implements ExperimentSelectionStrategy {

  private static Logger logger =
      LoggerFactory.getLogger(CompleteSelectionStrategy.class.getSimpleName());

  private final MinioService minioService;
  private final ExplorationModelDAO explorationModelDAO;
  private final BenchFlowTestModelDAO testModelDAO;

  public CompleteSelectionStrategy() {

    this.minioService = BenchFlowTestManagerApplication.getMinioService();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
  }

  // only used for testing
  public CompleteSelectionStrategy(MinioService minioService,
      ExplorationModelDAO explorationModelDAO, BenchFlowTestModelDAO testModelDAO) {
    this.minioService = minioService;
    this.explorationModelDAO = explorationModelDAO;
    this.testModelDAO = testModelDAO;
  }

  @Override
  public String selectNextExperiment(String testID) {

    logger.info("selectNextExperiment: " + testID);

    try {

      String testDefinitionYamlString =
          IOUtils.toString(minioService.getTestDefinition(testID), StandardCharsets.UTF_8);

      // get exploration space
      // TODO - generalize this to complete search space
      List<Integer> explorationSpace = explorationModelDAO.getWorkloadUserSpace(testID);

      // check which experiments have been executed
      Set<Long> executedExperimentNumbers = testModelDAO.getExperimentNumbers(testID);

      // expects that experiment has already been added to DB
      int nextExperimentNumber = executedExperimentNumbers.size() - 1;

      // select next experiment to execute
      Integer nextUserConfig = explorationSpace.get(nextExperimentNumber);

      logger.info("selectNextExperiment: number: " + nextExperimentNumber);

      // generate Experiment YAML file
      return BenchFlowDSL.experimentYamlBuilderFromTestYaml(testDefinitionYamlString)
          .numUsers(nextUserConfig).build();

    } catch (IOException | BenchFlowTestIDDoesNotExistException
        | BenchFlowDeserializationException e) {
      // should not happen
      // TODO - handle me
      e.printStackTrace();
    }

    return null;
  }

  public boolean isTestComplete(String testID) throws BenchFlowTestIDDoesNotExistException {
    // get exploration space
    // TODO - generalize this to complete search space
    List<Integer> explorationSpace = explorationModelDAO.getWorkloadUserSpace(testID);
    // check which experiments have been executed
    Set<Long> executedExperimentNumbers = testModelDAO.getExperimentNumbers(testID);

    return explorationSpace.size() == executedExperimentNumbers.size();
  }
}
