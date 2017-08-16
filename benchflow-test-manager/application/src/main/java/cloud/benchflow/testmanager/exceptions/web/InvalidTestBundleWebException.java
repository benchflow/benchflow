package cloud.benchflow.testmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17.
 */
public class InvalidTestBundleWebException extends WebApplicationException {

  public static String message = "Invalid BenchFlow Test Bundle";

  public InvalidTestBundleWebException() {
    super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).type("text/plain").build());
  }

  public InvalidTestBundleWebException(String info) {
    super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(message + ": " + info)
        .type("text/plain").build());
  }
}
