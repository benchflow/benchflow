package cloud.benchflow.faban.client.exceptions;

/**
 * Created by simonedavico on 28/10/15.
 *
 * @author vincenzoferme
 *
 *         Exception so that the client has to decide how to handle the case, according to its
 *         business logic
 */
public class IllegalRunStatusException extends FabanClientException {

  private String illegalRunStatus;

  public IllegalRunStatusException(String s) {
    super(s);
  }

  public IllegalRunStatusException(String s, String runStatus) {
    super(s);
    this.illegalRunStatus = runStatus;
  }

  public String getIllegalRunStatus() {
    return illegalRunStatus;
  }
}
