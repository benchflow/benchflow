package cloud.benchflow.testmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17.
 */
public class InvalidTrialIDWebException extends WebApplicationException {

  private static String message = "Invalid Trial ID";

  public InvalidTrialIDWebException() {
    super(message);
  }
}
