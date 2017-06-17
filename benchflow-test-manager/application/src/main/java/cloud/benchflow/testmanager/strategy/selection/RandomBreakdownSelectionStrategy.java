package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.dsl.ExplorationSpace;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-05
 */
public class RandomBreakdownSelectionStrategy implements SelectionStrategy {

  private static Logger logger =
      LoggerFactory.getLogger(RandomBreakdownSelectionStrategy.class.getSimpleName());

  private final MinioService minioService;
  private final ExplorationModelDAO explorationModelDAO;

  public RandomBreakdownSelectionStrategy() {

    this.minioService = BenchFlowTestManagerApplication.getMinioService();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  // only used for testing
  public RandomBreakdownSelectionStrategy(MinioService minioService,
      ExplorationModelDAO explorationModelDAO) {
    this.minioService = minioService;
    this.explorationModelDAO = explorationModelDAO;
  }

  @Override
  public SelectedExperimentBundle selectNextExperiment(String testID) {

    logger.info("selectNextExperiment: " + testID);

    try {

      // get executed exploration points
      List<Integer> explorationPointIndices =
          explorationModelDAO.getExecutedExplorationPointIndices(testID);

      String testDefinitionYamlString =
          IOUtils.toString(minioService.getTestDefinition(testID), StandardCharsets.UTF_8);

      String deploymentDescriptorYamlString = IOUtils
          .toString(minioService.getTestDeploymentDescriptor(testID), StandardCharsets.UTF_8);

      //      JavaCompatExplorationSpace explorationSpace = explorationModelDAO.getExplorationSpace(testID);
      ExplorationSpaceGenerator.ExplorationSpace explorationSpace =
          ExplorationSpace.explorationSpaceFromTestYaml(testDefinitionYamlString);

      // next experiment to be executed
      int nextExplorationPoint =
          getNextExplorationPoint(explorationSpace.size(), explorationPointIndices);

      // get the experiment bundle for the given point
      // <ExperimentDefinition, DeploymentDescriptor>
      Tuple2<String, String> experimentBundle =
          ExplorationSpace.generateExperimentBundle(explorationSpace, nextExplorationPoint,
              testDefinitionYamlString, deploymentDescriptorYamlString);

      return new SelectedExperimentBundle(experimentBundle._1(), // experiment definition
          experimentBundle._2(), // deployment descriptor
          nextExplorationPoint);


    } catch (BenchFlowTestIDDoesNotExistException | BenchFlowDeserializationException
        | IOException e) {
      // should not happen
      // TODO - handle me
      e.printStackTrace();
    }

    return null;
  }

  private int getNextExplorationPoint(int upperBound, List<Integer> explorationPointIndices) {

    Random random = new Random();

    int nextIndex = random.nextInt(upperBound);

    while (explorationPointIndices.contains(nextIndex)) {
      nextIndex = random.nextInt(upperBound);
    }

    return nextIndex;

  }

}
