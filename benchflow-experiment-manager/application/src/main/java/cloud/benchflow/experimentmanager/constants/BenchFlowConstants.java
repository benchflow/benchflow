package cloud.benchflow.experimentmanager.constants;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-03-21
 */
public class BenchFlowConstants {

  // REST API
  public static final String VERSION_1_PATH = "/v1";
  public static final String USERS_PATH = "/users/";
  public static final String TESTS_PATH = "/tests/";
  public static final String EXPERIMENTS_PATH = "/experiments/";
  public static final String TRIALS_PATH = "/trials/";
  // Minio
  public static final String DEPLOYMENT_DESCRIPTOR_NAME = "docker-compose";
  public static final String PT_PE_DEFINITION_NAME = "benchflow-test";
  public static final String BPMN_MODELS_FOLDER_NAME = "models";
  public static final String GENERATED_BENCHMARK_FILENAME = "benchflow-benchmark.jar";
  public static final String FABAN_CONFIG_FILENAME = "run.xml";
  public static final String MINIO_ID_DELIMITER = "/";
  // TODO - is this correct with only one bucket and it is called tests? Maybe better
  // then just 'benchflow'?
  public static final String TESTS_BUCKET = "tests";
  // MongoDB
  public static final String DB_NAME = "benchflow-experiment-manager";
  public static final String MODEL_ID_DELIMITER = ".";
  public static final String MODEL_ID_DELIMITER_REGEX = "\\.";
  // Faban
  public static final String FABAN_ID_DELIMITER = "_";
  public static final String TEMP_DIR = "./tmp";
  public static final String FABAN_CONFIGURATION_FILENAME = "run.xml";
  // TODO - put in common library so they can be handled by client
  // Exceptions
  public static final String INVALID_EXPERIMENT_ID_MESSAGE = "Invalid Experiment ID";
  private static final String YAML_EXTENSION = ".yml";
  public static final String PT_PE_DEFINITION_FILE_NAME = PT_PE_DEFINITION_NAME + YAML_EXTENSION;
  public static final String DEPLOYMENT_DESCRIPTOR_FILE_NAME =
      DEPLOYMENT_DESCRIPTOR_NAME + YAML_EXTENSION;

  public static String getExperimentID(String username, String testName, int testNumber,
      int experimentNumber) {
    return username + MODEL_ID_DELIMITER + testName + MODEL_ID_DELIMITER + testNumber
        + MODEL_ID_DELIMITER + experimentNumber;
  }

  public static String getPathFromExperimentID(String experimentID) {

    String[] experimentIDArray = experimentID.split(MODEL_ID_DELIMITER_REGEX);
    String username = experimentIDArray[0];
    String testName = experimentIDArray[1];
    String testNumber = experimentIDArray[2];
    String experimentNumber = experimentIDArray[3];

    return VERSION_1_PATH + USERS_PATH + username + TESTS_PATH + testName + "/" + testNumber
        + EXPERIMENTS_PATH + experimentNumber;
  }

  public static String getPathFromTrialID(String trialID) {

    String[] trialIDArray = trialID.split(MODEL_ID_DELIMITER_REGEX);
    String username = trialIDArray[0];
    String testName = trialIDArray[1];
    String testNumber = trialIDArray[2];
    String experimentNumber = trialIDArray[3];
    String trialNumber = trialIDArray[4];

    return VERSION_1_PATH + USERS_PATH + username + TESTS_PATH + testName + "/" + testNumber
        + EXPERIMENTS_PATH + experimentNumber + TRIALS_PATH + trialNumber;
  }

  public static int getTrialNumberFromTrialID(String trialID) {

    String[] trialIDArray = trialID.split(MODEL_ID_DELIMITER_REGEX);

    String trialNumber = trialIDArray[4];

    return Integer.parseInt(trialNumber);
  }

  public static String getTestNameFromTrialID(String trialID) {

    String[] trialIDArray = trialID.split(MODEL_ID_DELIMITER_REGEX);

    return trialIDArray[1];

  }

  public static String getTrialID(String experimentID, long trialNumber) {
    return experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + trialNumber;
  }

  public static String getExperimentIDFromTrialID(String trialID) {

    return trialID.substring(0, trialID.lastIndexOf(MODEL_ID_DELIMITER));
  }

  public static int getExperimentNumberFromTrialID(String trialID) {

    String[] trialIDArray = trialID.split(MODEL_ID_DELIMITER_REGEX);
    String experimentNumber = trialIDArray[3];

    return Integer.parseInt(experimentNumber);
  }

}
