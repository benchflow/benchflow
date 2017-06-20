package cloud.benchflow.faban.client.responses;

import cloud.benchflow.faban.client.exceptions.IllegalRunStatusException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 28/10/15.
 */
public class RunStatus implements Response {

  private StatusCode status;


  /**
   * Construct a run status.
   *
   * @param statusCode the status code
   * @param runId the run id
   */
  public RunStatus(String statusCode, RunId runId) {
    switch (statusCode.replace("\n", "")) {
      case "QUEUED":
        this.status = StatusCode.QUEUED;
        break;
      case "RECEIVED":
        this.status = StatusCode.RECEIVED;
        break;
      case "STARTED":
        this.status = StatusCode.STARTED;
        break;
      case "COMPLETED":
        this.status = StatusCode.COMPLETED;
        break;
      case "FAILED":
        this.status = StatusCode.FAILED;
        break;
      case "KILLED":
        this.status = StatusCode.KILLED;
        break;
      case "KILLING":
        this.status = StatusCode.KILLING;
        break;
      case "DENIED":
        this.status = StatusCode.DENIED;
        break;
      default:
        throw new IllegalRunStatusException(
            "RunId " + runId + "returned illegal run status " + statusCode);
    }
  }

  public StatusCode getStatus() {
    return this.status;
  }

  /**
   * Possible run statuses.
   */
  public enum StatusCode {
    QUEUED, RECEIVED, STARTED, COMPLETED, FAILED, KILLED, KILLING, DENIED
  }


}
