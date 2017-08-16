package cloud.benchflow.testmanager.strategy.validation;

import cloud.benchflow.dsl.ExplorationSpaceAPI;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpace;
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

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-05
 */
public class CompleteExplorationValidationStrategy implements ValidationStrategy {

  private static Logger logger =
      LoggerFactory.getLogger(CompleteExplorationValidationStrategy.class.getSimpleName());

  private final ExplorationModelDAO explorationModelDAO;
  private final MinioService minioService;

  public CompleteExplorationValidationStrategy() {
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    this.minioService = BenchFlowTestManagerApplication.getMinioService();
  }

  @Override
  public boolean isTestComplete(String testID) {

    logger.info("isTestComplete: " + testID);

    try {

      // get exploration space
      // TODO - investigate why there is an error calling this method
      // (e.g. instantiating scala case class)
      // JavaCompatExplorationSpace explorationSpace = explorationModelDAO
      // .getExplorationSpace(testID).toJavaCompat();

      String testDefinition =
          IOUtils.toString(minioService.getTestDefinition(testID), StandardCharsets.UTF_8);

      JavaCompatExplorationSpace explorationSpace =
          ExplorationSpaceAPI.explorationSpaceFromTestYaml(testDefinition);

      // get the executed exploration points
      List<Integer> explorationPointIndices =
          explorationModelDAO.getExecutedExplorationPointIndices(testID);

      // if the exploration size equals the number executed points it is complete
      return explorationSpace.size() == explorationPointIndices.size();

    } catch (BenchFlowTestIDDoesNotExistException | IOException
        | BenchFlowDeserializationException e) {
      // should not happen
      // TODO - handle me
      e.printStackTrace();
    }

    // in case something went wrong we should stop the test execution
    return true;
  }
}
