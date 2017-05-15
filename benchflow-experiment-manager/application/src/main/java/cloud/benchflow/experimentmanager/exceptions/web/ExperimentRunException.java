package cloud.benchflow.experimentmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 11/12/15.
 */
public class ExperimentRunException extends WebApplicationException {

  public ExperimentRunException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExperimentRunException(String message) {
    super(message);
  }
}
