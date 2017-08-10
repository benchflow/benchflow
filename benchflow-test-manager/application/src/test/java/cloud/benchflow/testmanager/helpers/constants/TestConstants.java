package cloud.benchflow.testmanager.helpers.constants;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.models.User;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 22.02.17.
 */
public class TestConstants {

  public static final String INVALID_TEST_NAME = "WfMSLoadTestInvalid";
  public static final String LOAD_TEST_NAME = "WfMSLoadTest";
  public static final String TEST_TERMINATION_CRITERIA_NAME = "TestTerminationCriteriaTest";
  public static final String VALID_TEST_NAME = LOAD_TEST_NAME;
  public static final String TEST_EXPLORATION_ONE_AT_A_TIME_USERS_NAME =
      "WfMSExhaustiveExplorationOneAtATimeSelectionUsersTest";
  public static final String TEST_EXPLORATION_ONE_AT_A_TIME_USERS__MEMORY_ENVIRONMENT_NAME =
      "WfMSExhaustiveExplorationOneAtATimeSelectionUsersMemoryEnvironMentTest";
  private static final String BENCHFLOW_USER_NAME = BenchFlowConstants.BENCHFLOW_USER.getUsername();
  public static String TEST_USER_NAME = "testUser";
  public static User TEST_USER = new User(TEST_USER_NAME);
  public static long VALID_TEST_NUMBER = 1;
  public static final String INVALID_TEST_BENCHFLOW_ID = BENCHFLOW_USER_NAME + MODEL_ID_DELIMITER
      + INVALID_TEST_NAME + MODEL_ID_DELIMITER + VALID_TEST_NUMBER;
  public static final String VALID_TEST_ID = BENCHFLOW_USER_NAME + MODEL_ID_DELIMITER
      + LOAD_TEST_NAME + MODEL_ID_DELIMITER + VALID_TEST_NUMBER;
  public static String LOAD_TEST_ID =
      TEST_USER_NAME + MODEL_ID_DELIMITER + LOAD_TEST_NAME + MODEL_ID_DELIMITER + VALID_TEST_NUMBER;
  public static String TEST_EXPLORATION_ONE_AT_A_TIME_USERS_ID = TEST_USER_NAME + MODEL_ID_DELIMITER
      + TEST_EXPLORATION_ONE_AT_A_TIME_USERS_NAME + MODEL_ID_DELIMITER + VALID_TEST_NUMBER;
  public static String TEST_EXPLORATION_ONE_AT_A_TIME_USERS__MEMORY_ENVIRONMENT_ID = TEST_USER_NAME
      + MODEL_ID_DELIMITER + TEST_EXPLORATION_ONE_AT_A_TIME_USERS__MEMORY_ENVIRONMENT_NAME
      + MODEL_ID_DELIMITER + VALID_TEST_NUMBER;
  private static long VALID_EXPERIMENT_NUMBER = 1;
  public static final String VALID_EXPERIMENT_ID =
      VALID_TEST_ID + MODEL_ID_DELIMITER + VALID_EXPERIMENT_NUMBER;
  public static String LOAD_EXPERIMENT_ID =
      LOAD_TEST_ID + MODEL_ID_DELIMITER + VALID_EXPERIMENT_NUMBER;
  private static long VALID_TRIAL_NUMBER = 1;
  public static String LOAD_TRIAL_ID = LOAD_EXPERIMENT_ID + MODEL_ID_DELIMITER + VALID_TRIAL_NUMBER;
}
