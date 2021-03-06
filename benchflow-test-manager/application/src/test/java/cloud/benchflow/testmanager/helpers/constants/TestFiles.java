package cloud.benchflow.testmanager.helpers.constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-27
 */
public class TestFiles {

  private static String LOCAL_TESTS_FOLDER = "src/test/resources/data/";

  private static String TEST_LOAD_FILE = LOCAL_TESTS_FOLDER + "definition/load/benchflow-test.yml";

  private static String TEST_EXPLORATION_ONE_AT_A_TIME_MEMORY_FILE = LOCAL_TESTS_FOLDER
      + "definition/exhaustive_exploration/one-at-a-time/memory/benchflow-test.yml";

  private static String TEST_EXPLORATION_ONE_AT_A_TIME_USERS_ENVIRONMENT_FILE = LOCAL_TESTS_FOLDER
      + "definition/exhaustive_exploration/one-at-a-time/users-environment/benchflow-test.yml";

  private static String TEST_EXPLORATION_ONE_AT_A_TIME_USERS_MEMORY_ENVIRONMENT_FILE =
      LOCAL_TESTS_FOLDER
          + "definition/exhaustive_exploration/one-at-a-time/users-memory-environment/benchflow-test.yml";

  private static String TEST_EXPLORATION_ONE_AT_A_TIME_USERS_FILE = LOCAL_TESTS_FOLDER
      + "definition/exhaustive_exploration/one-at-a-time/users/benchflow-test.yml";

  private static String TEST_EXPLORATION_RANDOM_USERS_FILE = LOCAL_TESTS_FOLDER
      + "definition/exhaustive_exploration/random_breakdown/users/benchflow-test.yml";

  private static String TEST_TERMINATION_CRITERIA_FILE =
      LOCAL_TESTS_FOLDER + "definition/test_termination/benchflow-test.yml";

  private static String TEST_STEP_USERS_FILE =
      LOCAL_TESTS_FOLDER + "definition/step/users/benchflow-test.yml";

  private static String TEST_DEPLOYMENT_DESCRIPTOR =
      LOCAL_TESTS_FOLDER + "deployment/docker-compose.yml";

  private static String TEST_MODELS_FOLDER_FILE = LOCAL_TESTS_FOLDER + "models";

  public static InputStream getTestLoadInputStream() throws FileNotFoundException {
    return new FileInputStream(TEST_LOAD_FILE);
  }

  public static File getTestLoadFile() {
    return new File(TEST_LOAD_FILE);
  }

  public static InputStream getTestExplorationOneAtATimeUsersMemoryEnvironmentInputStream()
      throws FileNotFoundException {

    return new FileInputStream(TEST_EXPLORATION_ONE_AT_A_TIME_USERS_MEMORY_ENVIRONMENT_FILE);
  }

  public static String getTestExplorationOneAtATimeUsersMemoryEnvironmentString()
      throws IOException {

    InputStream inputStream =
        new FileInputStream(TEST_EXPLORATION_ONE_AT_A_TIME_USERS_MEMORY_ENVIRONMENT_FILE);

    return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
  }

  public static File getTestExplorationOneAtATimeMemoryFile() throws FileNotFoundException {

    return new File(TEST_EXPLORATION_ONE_AT_A_TIME_MEMORY_FILE);
  }

  public static File getTestExplorationOneAtATimeUsersFile() throws FileNotFoundException {

    return new File(TEST_EXPLORATION_ONE_AT_A_TIME_USERS_FILE);
  }

  public static InputStream getTestExplorationOneAtATimeUsersInputStream()
      throws FileNotFoundException {

    return new FileInputStream(TEST_EXPLORATION_ONE_AT_A_TIME_USERS_FILE);
  }

  public static String getTestExplorationOneAtATimeUsersString() throws IOException {

    return IOUtils.toString(getTestExplorationOneAtATimeUsersInputStream(), StandardCharsets.UTF_8);
  }

  public static File getTestExplorationOneAtATimeUsersEnvironmentFile()
      throws FileNotFoundException {

    return new File(TEST_EXPLORATION_ONE_AT_A_TIME_USERS_ENVIRONMENT_FILE);
  }

  public static File getTestExplorationRandomUsersFile() throws FileNotFoundException {

    return new File(TEST_EXPLORATION_RANDOM_USERS_FILE);
  }

  public static InputStream getTestExplorationRandomUsersInputStream()
      throws FileNotFoundException {

    return new FileInputStream(TEST_EXPLORATION_RANDOM_USERS_FILE);
  }

  public static InputStream getTestTerminationCriteriaInputStream() throws FileNotFoundException {

    return new FileInputStream(TEST_TERMINATION_CRITERIA_FILE);
  }

  public static File getTestTerminationCriteriaFile() throws FileNotFoundException {

    return new File(TEST_TERMINATION_CRITERIA_FILE);
  }

  public static File getTestStepUsersFile() throws FileNotFoundException {

    return new File(TEST_STEP_USERS_FILE);
  }

  public static InputStream getDeploymentDescriptor() throws FileNotFoundException {

    return new FileInputStream(TEST_DEPLOYMENT_DESCRIPTOR);
  }

  public static File getTestDeploymentDescriptorFile() {
    return new File(TEST_DEPLOYMENT_DESCRIPTOR);
  }

  public static File getModelsFolderFile() {
    return new File(TEST_MODELS_FOLDER_FILE);
  }
}
