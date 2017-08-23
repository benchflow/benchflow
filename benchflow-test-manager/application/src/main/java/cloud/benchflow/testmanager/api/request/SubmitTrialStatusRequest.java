package cloud.benchflow.testmanager.api.request;

import cloud.benchflow.faban.client.responses.RunStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 16.02.17.
 */
public class SubmitTrialStatusRequest {

  @NotNull
  @JsonProperty
  private RunStatus.StatusCode status;

  public SubmitTrialStatusRequest() {}

  public SubmitTrialStatusRequest(RunStatus.StatusCode status) {
    this.status = status;
  }

  public RunStatus.StatusCode getStatus() {
    return status;
  }

  public void setStatus(RunStatus.StatusCode status) {
    this.status = status;
  }
}
