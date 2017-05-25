package cloud.benchflow.experimentmanager.helpers;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-13
 */
public class BenchFlowData {

  private static int EXPERIMENT_NUMBER = 1;

  public static String VALID_TEST_ID_1_TRIAL =
      "benchflow.ParallelMultiple11Activiti5210Test1Trial.1";
  public static String VALID_TEST_ID_2_TRIAL =
      "benchflow.ParallelMultiple11Activiti5210Test2Trial.1";

  public static final String VALID_EXPERIMENT_ID_1_TRIAL =
      VALID_TEST_ID_1_TRIAL + BenchFlowConstants.MODEL_ID_DELIMITER + EXPERIMENT_NUMBER;
  public static final String VALID_EXPERIMENT_ID_2_TRIAL =
      VALID_TEST_ID_2_TRIAL + BenchFlowConstants.MODEL_ID_DELIMITER + EXPERIMENT_NUMBER;

  public static final String INVALID_EXPERIMENT_ID = "benchflow.invalid.1.1";

  public static String getValidExperimentID2TrialFromNumber(int experimentNumber) {

    return VALID_TEST_ID_2_TRIAL + BenchFlowConstants.MODEL_ID_DELIMITER + experimentNumber;

  }

}