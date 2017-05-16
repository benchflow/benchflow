package cloud.benchflow.testmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17.
 */
public class InvalidBenchFlowTestIDWebException extends WebApplicationException {

  // TODO - check visability
  // TODO - check how to include message
  public static final String message = "Invalid BenchFlow Test ID";

  public InvalidBenchFlowTestIDWebException() {

    super(Response.status(Response.Status.NOT_FOUND).build());
  }
}
