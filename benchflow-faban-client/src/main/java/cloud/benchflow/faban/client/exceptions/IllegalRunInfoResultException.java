package cloud.benchflow.faban.client.exceptions;

/**
 * @author vincenzoferme
 *
 * Throwable so that the client has to decide how to handle the case, according to its
 * business logic
 */
public class IllegalRunInfoResultException extends FabanClientThrowable {

  String illegalResult;

  public IllegalRunInfoResultException(String s) {
    super(s);
  }

  public IllegalRunInfoResultException(String s, String result) {
    super(s);
    this.illegalResult = result;
  }

  public String getIllegalResult() {
    return illegalResult;
  }
}
