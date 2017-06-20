package cloud.benchflow.faban.client.exceptions;

/**
 * Created by simonedavico on 28/10/15.
 */
public class MalformedURIRuntimeException extends FabanClientRuntimeException {

  public MalformedURIRuntimeException(String message) {
    super(message);
  }

  public MalformedURIRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
