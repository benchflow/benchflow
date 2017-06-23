package cloud.benchflow.faban.client.exceptions;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 29/10/15.
 * @author vincenzoferme
 *
 *         Exception so that the client has to decide how to handle the case, according to its
 *         business logic
 */
public class EmptyHarnessResponseException extends FabanClientException {

  public EmptyHarnessResponseException() {
    super();
  }

  public EmptyHarnessResponseException(String message) {
    super(message);
  }

  public EmptyHarnessResponseException(String message, Throwable cause) {
    super(message, cause);
  }
}
