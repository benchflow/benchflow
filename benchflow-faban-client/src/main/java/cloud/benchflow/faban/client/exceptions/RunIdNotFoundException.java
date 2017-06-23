package cloud.benchflow.faban.client.exceptions;

/**
 *
 *
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 29/10/15.
 * @author vincenzoferme
 *
 *         Exception so that the client has to decide how to handle the case, according to its
 *         business logic
 */
public class RunIdNotFoundException extends FabanClientException {

  public RunIdNotFoundException(String message) {
    super(message);
  }

  public RunIdNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
