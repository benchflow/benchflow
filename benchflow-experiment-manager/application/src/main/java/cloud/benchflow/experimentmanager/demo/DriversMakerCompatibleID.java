package cloud.benchflow.experimentmanager.demo;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.tasks.experiment.ExperimentReadyTask;

/**
 * NOTE: This class is to be removed when driver-maker updates its minio interaction
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class DriversMakerCompatibleID {
  private String experimentName;
  private long experimentNumber;
  private String driversMakerExperimentID;

  public DriversMakerCompatibleID(String experimentID) {

    // userID = "BenchFlow"
    // ExperimentID := userId.experimentName.experimentNumber

    String[] experimentIDArray = experimentID.split(BenchFlowConstants.MODEL_ID_DELIMITER_REGEX);

    // use "-" so that it doesn't conflict with previous convention and other delimiters used
    experimentName = experimentIDArray[1] + "-" + experimentIDArray[2];
    experimentNumber = Long.parseLong(experimentIDArray[3]);
    driversMakerExperimentID = "BenchFlow." + experimentName;
  }

  public String getExperimentName() {
    return experimentName;
  }

  public long getExperimentNumber() {
    return experimentNumber;
  }

  public String getDriversMakerExperimentID() {
    return driversMakerExperimentID;
  }
}
