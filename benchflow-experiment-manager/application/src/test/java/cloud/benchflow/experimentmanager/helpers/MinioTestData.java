package cloud.benchflow.experimentmanager.helpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-13 */
public class MinioTestData {

  private static final String EXPERIMENT_DEFINITION_FILENAME =
      "src/test/resources/data/ParallelMultiple11/benchflow-test.yml";
  private static final String DEPLOYMENT_DESCRIPTOR_FILENAME =
      "src/test/resources/data/ParallelMultiple11/docker-compose.yml";
  private static final String BPM_MODEL_11_PARALLEL_FILENAME =
      "src/test/resources/data/ParallelMultiple11/models/11ParallelStructured.bpmn";
  private static final String GENERATED_BENCHMARK_FILENAME =
      "src/test/resources/data/ParallelMultiple11/benchflow-benchmark.jar";
  private static final String FABAN_CONFIGURATION_FILENAME =
      "src/test/resources/data/ParallelMultiple11/1/run.xml";

  public static final String BPM_MODEL_11_PARALLEL_NAME = "11ParallelStructured.bpmn";

  public static InputStream getExperimentDefinition() throws FileNotFoundException {
    return new FileInputStream(EXPERIMENT_DEFINITION_FILENAME);
  }

  public static InputStream getDeploymentDescriptor() throws FileNotFoundException {
    return new FileInputStream(DEPLOYMENT_DESCRIPTOR_FILENAME);
  }

  public static InputStream get11ParallelStructuredModel() throws FileNotFoundException {
    return new FileInputStream(BPM_MODEL_11_PARALLEL_FILENAME);
  }

  public static InputStream getGeneratedBenchmark() throws FileNotFoundException {
    return new FileInputStream(GENERATED_BENCHMARK_FILENAME);
  }

  public static InputStream getFabanConfiguration() throws FileNotFoundException {
    return new FileInputStream(FABAN_CONFIGURATION_FILENAME);
  }
}
