package cloud.benchflow.testmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17.
 */
public class InvalidTestBundleWebException extends WebApplicationException {

  private static String message = "Invalid BenchFlow Test Bundle";

  public InvalidTestBundleWebException() {
    super(message);
  }
}
