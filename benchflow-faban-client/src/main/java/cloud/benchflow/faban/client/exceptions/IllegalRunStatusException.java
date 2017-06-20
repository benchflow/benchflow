package cloud.benchflow.faban.client.exceptions;

/**
 * Created by simonedavico on 28/10/15.
 * @author vincenzoferme
 *
 * Throwable so that the client has to decide how to handle the case, according to its
 * business logic
 */
public class IllegalRunStatusException extends FabanClientThrowable {

  public IllegalRunStatusException(String s) {
    super(s);
  }
}
