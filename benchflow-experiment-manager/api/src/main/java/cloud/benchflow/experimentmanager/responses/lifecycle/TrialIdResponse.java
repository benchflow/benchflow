package cloud.benchflow.experimentmanager.responses.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 11/12/15.
 */
public class TrialIdResponse {

  @JsonProperty("benchmarkName")
  private String benchmarkName;

  @JsonProperty("experimentNumber")
  private long experimentNumber;

  @JsonProperty("trialNumber")
  private int trialNumber;

  @JsonProperty("trialId")
  private String id;

  public TrialIdResponse(String benchmarkName, long experimentNumber, int trialNumber) {
    this.benchmarkName = benchmarkName;
    this.experimentNumber = experimentNumber;
    this.trialNumber = trialNumber;
    this.id = benchmarkName + "." + experimentNumber + "." + trialNumber;
  }

  public String getBenchmarkName() {
    return benchmarkName;
  }

  public void setBenchmarkName(String benchmarkName) {
    this.benchmarkName = benchmarkName;
  }

  public long getExperimentNumber() {
    return experimentNumber;
  }

  public void setExperimentNumber(long experimentNumber) {
    this.experimentNumber = experimentNumber;
  }

  public int getTrialNumber() {
    return trialNumber;
  }

  public void setTrialNumber(int trialNumber) {
    this.trialNumber = trialNumber;
  }

  public String getId() {
    return id;
  }
}
