package cloud.benchflow.faban.client.exceptions;

/**
 * @author vincenzoferme
 *
 * Throwable so that the client has to decide how to handle the case, according to its
 * business logic
 */
public class IllegalRunInfoException extends FabanClientThrowable {

  public IllegalRunInfoException(String s) {
    super(s);
  }
}
