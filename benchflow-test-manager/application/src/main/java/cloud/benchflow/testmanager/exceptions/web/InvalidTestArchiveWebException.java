package cloud.benchflow.testmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17. */
public class InvalidTestArchiveWebException extends WebApplicationException {

  private static String message = "Invalid BenchFlow Test archive";

  public InvalidTestArchiveWebException() {
    super(message);
  }
}
