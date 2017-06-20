package cloud.benchflow.faban.client.exceptions;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p/>
 *         Created on 29/10/15.
 * @author vincenzoferme
 *
 *         Throwable so that the client has to decide how to handle the case, according to its
 *         business logic
 */
public class EmptyHarnessResponseRuntimeException extends FabanClientThrowable {

  public EmptyHarnessResponseRuntimeException() {
    super();
  }

  public EmptyHarnessResponseRuntimeException(String message) {
    super(message);
  }

  public EmptyHarnessResponseRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
