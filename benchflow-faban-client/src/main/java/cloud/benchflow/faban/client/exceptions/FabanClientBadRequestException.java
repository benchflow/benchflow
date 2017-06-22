package cloud.benchflow.faban.client.exceptions;

/**
 * @author vincenzoferme.
 */
public class FabanClientBadRequestException extends FabanClientThrowable {

  public FabanClientBadRequestException() {}

  public FabanClientBadRequestException(String message) {
    super(message);
  }

  public FabanClientBadRequestException(String message, Throwable cause) {
    super(message, cause);
  }

}
