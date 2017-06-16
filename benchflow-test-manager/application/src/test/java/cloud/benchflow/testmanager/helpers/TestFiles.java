package cloud.benchflow.testmanager.helpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-27
 */
public class TestFiles {

  private static String LOCAL_TESTS_FOLDER = "src/test/resources/data/";

  private static String TEST_EXPLORATION_ONE_AT_A_TIME_USERS_MEMORY_ENVIRONMENT_FILE =
      LOCAL_TESTS_FOLDER + "exploration/one-at-a-time/users-memory-environment/benchflow-test.yml";

  private static String TEST_EXPLORATION_ONE_AT_A_TIME_USERS_FILE =
      LOCAL_TESTS_FOLDER + "exploration/one-at-a-time/users/benchflow-test.yml";

  private static String TEST_EXPLORATION_RANDOM_USERS_FILE =
      LOCAL_TESTS_FOLDER + "exploration/random_breakdown/users/benchflow-test.yml";

  private static String TEST_DEPLOYMENT_DESCRIPTOR =
      LOCAL_TESTS_FOLDER + "deployment/docker-compose.yml";

  public static InputStream getTestExplorationOneAtATimeUsersMemoryEnvironmentInputStream()
      throws FileNotFoundException {

    return new FileInputStream(TEST_EXPLORATION_ONE_AT_A_TIME_USERS_MEMORY_ENVIRONMENT_FILE);
  }

  public static InputStream getTestExplorationOneAtATimeUsersInputStream()
      throws FileNotFoundException {

    return new FileInputStream(TEST_EXPLORATION_ONE_AT_A_TIME_USERS_FILE);
  }

  public static InputStream getTestExplorationRandomUsersInputStream()
      throws FileNotFoundException {

    return new FileInputStream(TEST_EXPLORATION_RANDOM_USERS_FILE);
  }

  public static InputStream getDeploymentDescriptor() throws FileNotFoundException {

    return new FileInputStream(TEST_DEPLOYMENT_DESCRIPTOR);
  }
}
