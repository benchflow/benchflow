package cloud.benchflow.experimentmanager.api.request;

import cloud.benchflow.faban.client.responses.RunInfo;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

public class FabanStatusRequest {

  @NotNull
  @JsonProperty
  private String trialID;
  @NotNull
  @JsonProperty
  private RunStatus.StatusCode statusCode;
  @NotNull
  @JsonProperty
  private RunInfo.Result result;

  public FabanStatusRequest(String trialID, RunStatus.StatusCode statusCode,
      RunInfo.Result result) {
    this.trialID = trialID;
    this.statusCode = statusCode;
    this.result = result;
  }

  public String getTrialID() {
    return trialID;
  }

  public RunStatus.StatusCode getStatusCode() {
    return statusCode;
  }

  public Result getResult() {
    return result;
  }
}
