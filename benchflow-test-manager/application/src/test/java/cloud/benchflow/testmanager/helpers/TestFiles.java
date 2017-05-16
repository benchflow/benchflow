package cloud.benchflow.testmanager.helpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-27
 */
public class TestFiles {

  private static String TESTS_FOLDER = "../../tests/data/dsl-examples/definition/benchflow-test/";

  private static String TEST_EXPLORATION_COMPLETE_USERS_FILE =
      TESTS_FOLDER + "exploration/complete/users/benchflow-test.yml";

  public static InputStream getTestExplorationCompleteUsersInputStream()
      throws FileNotFoundException {

    return new FileInputStream(TEST_EXPLORATION_COMPLETE_USERS_FILE);
  }
}
