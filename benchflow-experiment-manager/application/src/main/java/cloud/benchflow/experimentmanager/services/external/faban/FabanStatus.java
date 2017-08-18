package cloud.benchflow.experimentmanager.services.external.faban;

import cloud.benchflow.faban.client.responses.RunInfo;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus;

public class FabanStatus {

  private String trialID;
  private RunStatus.StatusCode statusCode;
  private RunInfo.Result result;

  public FabanStatus(String trialID, RunStatus.StatusCode statusCode, RunInfo.Result result) {
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
