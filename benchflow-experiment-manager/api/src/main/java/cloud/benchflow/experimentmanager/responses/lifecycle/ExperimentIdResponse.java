package cloud.benchflow.experimentmanager.responses.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 28/06/16.
 */
public class ExperimentIdResponse {

  @JsonProperty("experimentId")
  private String experimentId;

  @JsonProperty("trials")
  private int trials;

  public ExperimentIdResponse(final String experimentId, final int trials) {
    this.experimentId = experimentId;
    this.trials = trials;
  }

  public String getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(String experimentId) {
    this.experimentId = experimentId;
  }

  public int getTrials() {
    return trials;
  }

  public void setTrials(int trials) {
    this.trials = trials;
  }
}
