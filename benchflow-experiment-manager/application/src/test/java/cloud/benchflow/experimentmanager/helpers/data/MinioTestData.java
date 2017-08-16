package cloud.benchflow.experimentmanager.helpers.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-13
 */
public class MinioTestData {

  public static final String BPMN_MODEL_TEST_NAME = "test.bpmn";
  public static final String BPMN_MODEL_MOCK_NAME = "mock.bpmn";

  private static final String EXPERIMENT_1_TRIAL_DEFINITION_FILENAME =
      "src/test/resources/data/experiments/1-trials/benchflow-test.yml";
  private static final String EXPERIMENT_2_TRIALS_DEFINITION_FILENAME =
      "src/test/resources/data/experiments/2-trials/benchflow-test.yml";
  private static final String EXPERIMENT_100_TRIALS_DEFINITION_FILENAME =
      "src/test/resources/data/experiments/100-trials/benchflow-test.yml";
  private static final String DEPLOYMENT_DESCRIPTOR_FILENAME =
      "src/test/resources/data/docker-compose.yml";
  private static final String BPM_MODEL_TEST_FILENAME =
      "src/test/resources/data/models/" + BPMN_MODEL_TEST_NAME;
  private static final String BPM_MODEL_MOCK_FILENAME =
      "src/test/resources/data/models/" + BPMN_MODEL_MOCK_NAME;
  private static final String GENERATED_BENCHMARK_FILENAME =
      "src/test/resources/data/benchflow-benchmark.jar";
  private static final String FABAN_CONFIGURATION_FILENAME = "src/test/resources/data/run.xml";

  public static InputStream getExperiment1TrialDefinition() throws FileNotFoundException {
    return new FileInputStream(EXPERIMENT_1_TRIAL_DEFINITION_FILENAME);
  }

  public static InputStream getExperiment2TrialsDefinition() throws FileNotFoundException {
    return new FileInputStream(EXPERIMENT_2_TRIALS_DEFINITION_FILENAME);
  }

  public static InputStream getExperiment100TrialsDefinition() throws FileNotFoundException {
    return new FileInputStream(EXPERIMENT_100_TRIALS_DEFINITION_FILENAME);
  }

  public static InputStream getDeploymentDescriptor() throws FileNotFoundException {
    return new FileInputStream(DEPLOYMENT_DESCRIPTOR_FILENAME);
  }

  public static InputStream getTestModel() throws FileNotFoundException {
    return new FileInputStream(BPM_MODEL_TEST_FILENAME);
  }

  public static InputStream getMockModel() throws FileNotFoundException {
    return new FileInputStream(BPM_MODEL_MOCK_FILENAME);
  }

  public static InputStream getGeneratedBenchmark() throws FileNotFoundException {
    return new FileInputStream(GENERATED_BENCHMARK_FILENAME);
  }

  public static InputStream getFabanConfiguration() throws FileNotFoundException {
    return new FileInputStream(FABAN_CONFIGURATION_FILENAME);
  }
}
