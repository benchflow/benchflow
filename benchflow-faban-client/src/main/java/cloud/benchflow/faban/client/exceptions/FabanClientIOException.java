package cloud.benchflow.faban.client.exceptions;

/**
 * Created by simonedavico on 27/10/15.
 * @author vincenzoferme
 */
public class FabanClientIOException extends FabanClientException {

  public FabanClientIOException() {}

  public FabanClientIOException(String message) {
    super(message);
  }

  public FabanClientIOException(String message, Throwable cause) {
    super(message, cause);
  }

}
