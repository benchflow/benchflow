package cloud.benchflow.faban.client.exceptions;

/**
 * Created by simonedavico on 27/10/15.
 */
public class FabanClientRuntimeException extends RuntimeException {

  public FabanClientRuntimeException() {}

  public FabanClientRuntimeException(String message) {
    super(message);
  }

  public FabanClientRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}
