package cloud.benchflow.faban.client.exceptions;

/**
 * @author vincenzoferme.
 */
public class FabanClientException extends Exception {

  public FabanClientException() {}

  public FabanClientException(String message) {
    super(message);
  }

  public FabanClientException(String message, Throwable cause) {
    super(message, cause);
  }

}
