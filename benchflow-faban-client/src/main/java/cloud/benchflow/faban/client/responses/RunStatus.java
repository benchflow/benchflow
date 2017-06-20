package cloud.benchflow.faban.client.responses;

import cloud.benchflow.faban.client.exceptions.IllegalRunStatusException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 28/10/15.
 */
public class RunStatus implements Response {

  private Code status;


  /**
   * Construct a run status.
   *
   * @param statusCode the status code
   * @param runId the run id
   */
  public RunStatus(String statusCode, RunId runId) {
    switch (statusCode.replace("\n", "")) {
      case "QUEUED":
        this.status = Code.QUEUED;
        break;
      case "RECEIVED":
        this.status = Code.RECEIVED;
        break;
      case "STARTED":
        this.status = Code.STARTED;
        break;
      case "COMPLETED":
        this.status = Code.COMPLETED;
        break;
      case "FAILED":
        this.status = Code.FAILED;
        break;
      case "KILLED":
        this.status = Code.KILLED;
        break;
      case "KILLING":
        this.status = Code.KILLING;
        break;
      case "DENIED":
        this.status = Code.DENIED;
        break;
      default:
        throw new IllegalRunStatusException(
            "RunId " + runId + "returned illegal run status " + statusCode);
    }
  }

  public Code getStatus() {
    return this.status;
  }

  /**
   * Possible run statuses.
   */
  public enum Code {
    QUEUED, RECEIVED, STARTED, COMPLETED, FAILED, KILLED, KILLING, DENIED
  }


}
