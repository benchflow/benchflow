package cloud.benchflow.testmanager.tasks.start;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;
import cloud.benchflow.testmanager.tasks.BenchFlowTestTaskController;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Prepares the test for running.
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public class StartTask implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(StartTask.class.getSimpleName());

  private final String testID;
  private final String testDefinitionYamlString;
  private final InputStream deploymentDescriptorInputStream;
  private final Map<String, InputStream> bpmnModelInputStreams;

  // services
  private final MinioService minioService;
  private final ExplorationModelDAO explorationModelDAO;
  private final BenchFlowTestTaskController testTaskController;

  public StartTask(
      String testID,
      String testDefinitionYamlString,
      InputStream deploymentDescriptorInputStream,
      Map<String, InputStream> bpmnModelInputStreams) {

    this.testID = testID;
    this.testDefinitionYamlString = testDefinitionYamlString;
    this.deploymentDescriptorInputStream = deploymentDescriptorInputStream;
    this.bpmnModelInputStreams = bpmnModelInputStreams;

    this.minioService = BenchFlowTestManagerApplication.getMinioService();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    this.testTaskController = BenchFlowTestManagerApplication.getTestTaskController();
  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    // extract contents
    InputStream definitionInputStream =
        IOUtils.toInputStream(testDefinitionYamlString, StandardCharsets.UTF_8);

    // TODO - handle different SUT types
    // TODO - check that termination criteria with time has not been exceeded

    // save PT archive contents to Minio
    minioService.saveTestDefinition(testID, definitionInputStream);
    minioService.saveTestDeploymentDescriptor(testID, deploymentDescriptorInputStream);

    bpmnModelInputStreams.forEach(
        (fileName, inputStream) -> minioService.saveTestBPMNModel(testID, fileName, inputStream));

    try {
      BenchFlowTest test = BenchFlowDSL.testFromYaml(testDefinitionYamlString);

      List<Integer> workloadUserSpace = generateExplorationSpace(test);

      if (workloadUserSpace != null) {
        explorationModelDAO.setWorkloadUserSpace(testID, workloadUserSpace);
      }

    } catch (BenchFlowDeserializationException | BenchFlowTestIDDoesNotExistException e) {
      // should not happen since it has already been tested/added
      logger.error("should not happen");
      e.printStackTrace();
    }

    logger.info("completed: " + testID);

    // send the test to the controller
    testTaskController.handleTestState(testID);
  }

  public static List<Integer> generateExplorationSpace(BenchFlowTest test) {

    // generate exploration space if any

    // TODO - replace this with calculating all possible combinations
    // something like this https://blog.balfes.net/2015/06/08/finding-every-possible-combination-of-array-entries-from-multiple-lists-with-unknown-bounds-in-java/

    if (test.configuration().goal().explorationSpace().isDefined()) {

      if (test.configuration().goal().explorationSpace().get().workload().isDefined()) {

        return JavaConverters.asJavaCollectionConverter(
                test.configuration()
                    .goal()
                    .explorationSpace()
                    .get()
                    .workload()
                    .get()
                    .users()
                    .get()
                    .values())
            .asJavaCollection()
            .stream()
            .map(object -> (Integer) object)
            .collect(Collectors.toList());
      }
    }

    return null;
  }
}
