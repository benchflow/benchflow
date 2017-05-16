package cloud.benchflow.testmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17.
 */
public class BenchFlowTestIDExistsWebException extends WebApplicationException {

  private static String message = "BenchFlow Test ID already exists";

  public BenchFlowTestIDExistsWebException() {
    super(message);
  }
}
