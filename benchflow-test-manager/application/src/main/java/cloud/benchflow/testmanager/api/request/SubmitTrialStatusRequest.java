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
  private RunStatus.Code status;

  public SubmitTrialStatusRequest() {}

  public SubmitTrialStatusRequest(RunStatus.Code status) {
    this.status = status;
  }

  public RunStatus.Code getStatus() {
    return status;
  }

  public void setStatus(RunStatus.Code status) {
    this.status = status;
  }
}
