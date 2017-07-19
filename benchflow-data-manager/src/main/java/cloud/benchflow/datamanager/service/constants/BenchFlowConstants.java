package cloud.benchflow.datamanager.service.constants;

public final class BenchFlowConstants {

  // REST API
  public static final String VERSION_1_PATH = "/v1";
  public static final String USERS_PATH = "/users/";
  public static final String TESTS_PATH = "/tests/";
  public static final String EXPERIMENTS_PATH = "/experiments/";
  public static final String TRIALS_PATH = "/trials/";
  // Archive
  public static final String DEPLOYMENT_DESCRIPTOR_NAME = "docker-compose";
  public static final String TEST_EXPERIMENT_DEFINITION_NAME = "benchflow-test";
  public static final String BPMN_MODELS_FOLDER_NAME = "models";
  public static final String MINIO_ID_DELIMITER = "/";
  // Minio
  public static final String TESTS_BUCKET = "tests";
  public static final String GENERATED_BENCHMARK_FILE_NAME = "benchflow-benchmark.jar";
  // MongoDB
  public static final String DB_NAME = "benchflow-test-manager";
  public static final String MODEL_ID_DELIMITER = ".";
  public static final String MODEL_ID_DELIMITER_REGEX = "\\.";
  // TODO - this is necessary for current version of Test Manager
  private static final String YAML_EXTENSION = ".yml";
  public static final String TEST_EXPERIMENT_DEFINITION_FILE_NAME =
      TEST_EXPERIMENT_DEFINITION_NAME + YAML_EXTENSION;
  public static final String DEPLOYMENT_DESCRIPTOR_FILE_NAME =
      DEPLOYMENT_DESCRIPTOR_NAME + YAML_EXTENSION;

  private BenchFlowConstants() {}

  public static String getTestID(String username, String testName, int testNumber) {
    return username + MODEL_ID_DELIMITER + testName + MODEL_ID_DELIMITER + testNumber;
  }

  public static String getExperimentID(String username, String testName, int testNumber,
      int experimentNumber) {
    return getTestID(username, testName, testNumber) + MODEL_ID_DELIMITER + experimentNumber;
  }

}
