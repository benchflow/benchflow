package cloud.benchflow.experimentmanager.helpers;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-13
 */
public class BenchFlowData {

  private static int EXPERIMENT_NUMBER = 1;

  public static String VALID_TEST_ID_1_TRIAL = "benchflow.TestExperiment1Trial.1";
  public static String VALID_TEST_ID_2_TRIAL = "benchflow.TestExperiment2Trials.1";

  public static String SCENARIO_ALWAYS_COMPLETED_EXPERIMENT_ID = "benchflow.alwaysCompleted.1.1";
  public static String SCENARIO_FAIL_FIRST_EXEC_EXPERIMENT_ID = "benchflow.failFirstExecution.1.1";
  public static String SCENARIO_ALWAYS_FAIL_EXPERIMENT_ID = "benchflow.alwaysFail.1.1";
  public static String SCENARIO_FAIL_EVERY_SECOND_EXPERIMENT_TEST_ID =
      "benchflow.failEverySecondExperiment.1";
  public static String NO_SCENARIO_EXPERIMENT_ID = "benchflow.noScenario.1.1";

  public static final String VALID_EXPERIMENT_ID_1_TRIAL =
      VALID_TEST_ID_1_TRIAL + BenchFlowConstants.MODEL_ID_DELIMITER + EXPERIMENT_NUMBER;
  public static final String VALID_EXPERIMENT_ID_2_TRIAL =
      VALID_TEST_ID_2_TRIAL + BenchFlowConstants.MODEL_ID_DELIMITER + EXPERIMENT_NUMBER;

  public static final String INVALID_EXPERIMENT_ID = "benchflow.invalid.1.1";

  public static String getValidExperimentID2TrialFromNumber(int experimentNumber) {

    return VALID_TEST_ID_2_TRIAL + BenchFlowConstants.MODEL_ID_DELIMITER + experimentNumber;

  }

}
