package cloud.benchflow.experimentmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 05/03/16.
 */
public class BenchmarkGenerationException extends WebApplicationException {

  public BenchmarkGenerationException(String message) {
    super(message, Response.Status.INTERNAL_SERVER_ERROR);
  }

  public BenchmarkGenerationException(String message, int status) {
    super(message, status);
  }

  public BenchmarkGenerationException(String message, Throwable cause) {
    super(message, cause, Response.Status.INTERNAL_SERVER_ERROR);
  }
}
