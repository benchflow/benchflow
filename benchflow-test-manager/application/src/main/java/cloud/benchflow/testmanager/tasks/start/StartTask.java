package cloud.benchflow.testmanager.tasks.start;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

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
  private final TestTaskScheduler testTaskController;

  public StartTask(String testID) {

    this.testID = testID;

    this.minioService = BenchFlowTestManagerApplication.getMinioService();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    this.testTaskController = BenchFlowTestManagerApplication.getTestTaskScheduler();
  }

  public static List<Integer> generateExplorationSpace(BenchFlowTest test) {

    // generate exploration space if any

    // TODO - replace this with calculating all possible combinations
    // something like this https://blog.balfes.net/2015/06/08/finding-every-possible-combination-of-array-entries-from-multiple-lists-with-unknown-bounds-in-java/

    if (test.configuration().goal().explorationSpace().isDefined()) {

      if (test.configuration().goal().explorationSpace().get().workload().isDefined()) {

        return JavaConverters
            .asJavaCollectionConverter(test.configuration().goal().explorationSpace().get()
                .workload().get().users().get().values())
            .asJavaCollection().stream().map(object -> (Integer) object)
            .collect(Collectors.toList());
      }
    }

    return null;
  }

  @Override
  public void run() {

    logger.info("running: " + testID);

    // TODO - handle different SUT types
    // TODO - check that termination criteria with time has not been exceeded

    try {

      String testDefinitionYamlString =
          IOUtils.toString(minioService.getTestDefinition(testID), StandardCharsets.UTF_8);

      BenchFlowTest test = BenchFlowDSL.testFromYaml(testDefinitionYamlString);

      List<Integer> workloadUserSpace = generateExplorationSpace(test);

      if (workloadUserSpace != null) {
        explorationModelDAO.setWorkloadUserSpace(testID, workloadUserSpace);
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
