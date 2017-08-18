package cloud.benchflow.experimentmanager.responses.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 09/04/16.
 */
public class  ExperimentStatusResponse {

  @JsonProperty("experimentId")
  private String experimentId;

  @JsonProperty("status")
  private String status;

  @JsonProperty("trials")
  private List<TrialStatusResponse> trialsStatus;

  public ExperimentStatusResponse(String experimentId, String status) {
    this.experimentId = experimentId;
    this.status = status;
  }

  public String getExperimentId() {
    return experimentId;
  }

  public void setExperimentId(String experimentId) {
    this.experimentId = experimentId;
  }

  public void setExperimentStatus(String status) {
    this.status = status;
  }

  public List<TrialStatusResponse> getTrialsStatus() {
    return trialsStatus;
  }

  public void setTrialsStatus(List<TrialStatusResponse> trialsStatus) {
    this.trialsStatus = trialsStatus;
  }

  public void addTrialStatus(TrialStatusResponse ts) {
    this.trialsStatus.add(ts);
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
