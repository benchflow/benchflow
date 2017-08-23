package cloud.benchflow.faban.client.exceptions;

/**
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public class FabanClientHttpResponseException extends FabanClientException {

  public FabanClientHttpResponseException() {}

  public FabanClientHttpResponseException(String message) {
    super(message);
  }

  public FabanClientHttpResponseException(String message, Throwable cause) {
    super(message, cause);
  }

}
