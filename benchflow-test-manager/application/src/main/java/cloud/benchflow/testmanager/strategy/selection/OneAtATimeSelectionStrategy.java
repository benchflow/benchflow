package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.dsl.ExplorationSpace;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public class OneAtATimeSelectionStrategy implements SelectionStrategy {

  private static Logger logger =
      LoggerFactory.getLogger(OneAtATimeSelectionStrategy.class.getSimpleName());

  private final MinioService minioService;
  private final ExplorationModelDAO explorationModelDAO;
  private final BenchFlowTestModelDAO testModelDAO;

  public OneAtATimeSelectionStrategy() {

    this.minioService = BenchFlowTestManagerApplication.getMinioService();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
  }

  // only used for testing
  public OneAtATimeSelectionStrategy(MinioService minioService,
      ExplorationModelDAO explorationModelDAO, BenchFlowTestModelDAO testModelDAO) {
    this.minioService = minioService;
    this.explorationModelDAO = explorationModelDAO;
    this.testModelDAO = testModelDAO;
  }

  @Override
  public SelectedExperimentBundle selectNextExperiment(String testID) {

    logger.info("selectNextExperiment: " + testID);

    try {

      // get executed exploration points
      List<Integer> explorationPointIndices =
          explorationModelDAO.getExecutedExplorationPointIndices(testID);

      // next experiment to be executed
      int nextExplorationPoint;
      // if list is empty we take the first
      if (explorationPointIndices.size() == 0) {
        nextExplorationPoint = 0;
      } else {
        // get the max index and add 1 for the next
        nextExplorationPoint =
            explorationPointIndices.stream().max(Integer::compareTo).orElse(0) + 1;
      }

      String testDefinitionYamlString =
          IOUtils.toString(minioService.getTestDefinition(testID), StandardCharsets.UTF_8);

      String deploymentDescriptorYamlString = IOUtils
          .toString(minioService.getTestDeploymentDescriptor(testID), StandardCharsets.UTF_8);

      //      JavaCompatExplorationSpace explorationSpace = explorationModelDAO.getExplorationSpace(testID);
      ExplorationSpaceGenerator.ExplorationSpace explorationSpace =
          ExplorationSpace.explorationSpaceFromTestYaml(testDefinitionYamlString);

      // get the experiment bundle for the given point
      // <ExperimentDefinition, DeploymentDescriptor>
      Tuple2<String, String> experimentBundle =
          ExplorationSpace.generateExperimentBundle(explorationSpace, nextExplorationPoint,
              testDefinitionYamlString, deploymentDescriptorYamlString);

      return new SelectedExperimentBundle(experimentBundle._1(), // experiment definition
          experimentBundle._2(), // deployment descriptor
          nextExplorationPoint);

    } catch (IOException | BenchFlowTestIDDoesNotExistException
        | BenchFlowDeserializationException e) {
      // should not happen
      // TODO - handle me
      e.printStackTrace();
    }

    return null;
  }

}
