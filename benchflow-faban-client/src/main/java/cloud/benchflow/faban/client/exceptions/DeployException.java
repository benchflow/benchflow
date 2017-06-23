package cloud.benchflow.faban.client.exceptions;


/**
 * Created by simonedavico on 28/10/15.
 *
 * @author vincenzoferme
 *
 *         Exception so that the client has to decide how to handle the case, according to its
 *         business logic
 */
public class DeployException extends FabanClientException {

  public DeployException(String message) {
    super(message);
  }

  public DeployException(String message, Throwable cause) {
    super(message, cause);
  }

}
