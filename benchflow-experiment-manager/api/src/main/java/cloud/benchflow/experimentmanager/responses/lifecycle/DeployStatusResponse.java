package cloud.benchflow.experimentmanager.responses.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 03/12/15.
 */
public class DeployStatusResponse {

  @JsonProperty("deploy")
  private String status;

  public DeployStatusResponse(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
