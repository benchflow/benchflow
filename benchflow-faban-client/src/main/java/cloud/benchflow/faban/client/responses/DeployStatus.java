package cloud.benchflow.faban.client.responses;

import cloud.benchflow.faban.client.exceptions.DeployException;
import org.apache.http.HttpStatus;


/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 */
public class DeployStatus implements Response {

  private Code code;


  /**
   * Construct a Deploy Status response.
   *
   * @param statusCode the deploy status code
   */
  public DeployStatus(int statusCode) throws DeployException {
    switch (statusCode) {
      case HttpStatus.SC_CREATED:
        this.code = Code.CREATED;
        break;
      case HttpStatus.SC_CONFLICT:
        this.code = Code.CONFLICT;
        break;
      case HttpStatus.SC_NOT_ACCEPTABLE:
        this.code = Code.NOT_ACCEPTABLE;
        break;
      default:
        throw new DeployException(
            "Deploy returned response with unexpected " + "status code " + statusCode);
    }
  }

  public Code getCode() {
    return this.code;
  }

  public enum Code {
    CONFLICT, NOT_ACCEPTABLE, CREATED
  }

}
