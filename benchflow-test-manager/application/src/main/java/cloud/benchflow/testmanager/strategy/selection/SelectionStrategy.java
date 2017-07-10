package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.dsl.ExplorationSpaceAPI;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.external.MinioService;
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
public abstract class SelectionStrategy {

  private final MinioService minioService;
  private final ExplorationModelDAO explorationModelDAO;
  private Logger logger = LoggerFactory.getLogger(SelectionStrategy.class.getSimpleName());

  public SelectionStrategy(Logger logger) {

    this.logger = logger;

    this.minioService = BenchFlowTestManagerApplication.getMinioService();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  // only used for testing
  public SelectionStrategy(Logger logger, MinioService minioService,
      ExplorationModelDAO explorationModelDAO) {
    this.logger = logger;
    this.minioService = minioService;
    this.explorationModelDAO = explorationModelDAO;
  }

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

      // JavaCompatExplorationSpace explorationSpace = explorationModelDAO.getExplorationSpace(testID);
      ExplorationSpaceGenerator.ExplorationSpace explorationSpace =
          ExplorationSpaceAPI.explorationSpaceFromTestYaml(testDefinitionYamlString);

      // next experiment to be executed
      int nextExplorationPoint =
          getNextExplorationPoint(explorationPointIndices, explorationSpace.size());

      // get the experiment bundle for the given point
      // <ExperimentDefinition, DeploymentDescriptor>
      Tuple2<String, String> experimentBundle =
          ExplorationSpaceAPI.generateExperimentBundle(explorationSpace, nextExplorationPoint,
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

  protected abstract int getNextExplorationPoint(List<Integer> executedExplorationPointIndices,
      int explorationSpaceSize);

  public class SelectedExperimentBundle {

    private String experimentYamlString;
    private String deploymentDescriptorYamlString;
    private int explorationSpaceIndex;

    public SelectedExperimentBundle(String experimentYamlString,
        String deploymentDescriptorYamlString, int explorationSpaceIndex) {
      this.experimentYamlString = experimentYamlString;
      this.deploymentDescriptorYamlString = deploymentDescriptorYamlString;
      this.explorationSpaceIndex = explorationSpaceIndex;
    }

    public String getExperimentYamlString() {
      return experimentYamlString;
    }

    public String getDeploymentDescriptorYamlString() {
      return deploymentDescriptorYamlString;
    }

    public int getExplorationSpaceIndex() {
      return explorationSpaceIndex;
    }
  }
}
