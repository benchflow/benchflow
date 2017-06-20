package cloud.benchflow.faban.client.exceptions;

/**
 * @author vincenzoferme
 */
public class FabanClientThrowable extends Throwable {

  public FabanClientThrowable() {}

  public FabanClientThrowable(String message) {
    super(message);
  }

  public FabanClientThrowable(String message, Throwable cause) {
    super(message, cause);
  }

}
