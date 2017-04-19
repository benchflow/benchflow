package cloud.benchflow.minioclient.helpers;

import static cloud.benchflow.minioclient.BenchFlowMinioClient.MODEL_ID_DELIMITER;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-19
 */
public class TestConstants {

    public static final String INVALID_BENCHFLOW_TEST_NAME = "invalid";
    public static final String VALID_BENCHFLOW_TEST_NAME = "testNameExample";

    public static String TEST_USER_NAME = "testUser";

    public static long VALID_TEST_NUMBER = 1;
    public static long VALID_EXPERIMENT_NUMBER = 1;
    public static long VALID_TRIAL_NUMBER = 1;

    public static String VALID_TEST_ID = TEST_USER_NAME + MODEL_ID_DELIMITER + VALID_BENCHFLOW_TEST_NAME + MODEL_ID_DELIMITER + VALID_TEST_NUMBER;
    public static String VALID_EXPERIMENT_ID = VALID_TEST_ID + MODEL_ID_DELIMITER + VALID_EXPERIMENT_NUMBER;
    public static String VALID_TRIAL_ID = VALID_EXPERIMENT_ID + MODEL_ID_DELIMITER + VALID_TRIAL_NUMBER;

}
