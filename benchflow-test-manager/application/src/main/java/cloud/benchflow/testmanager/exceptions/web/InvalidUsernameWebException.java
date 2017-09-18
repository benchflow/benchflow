package cloud.benchflow.testmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17.
 */
public class InvalidUsernameWebException extends WebApplicationException {

  public static final String message = "Invalid Username";

  public InvalidUsernameWebException() {

    super(Response.status(Response.Status.NOT_FOUND).entity(message).type("text/plain").build());
  }
}
