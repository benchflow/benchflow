package cloud.benchflow.experimentmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 09/04/16.
 */
public class NoSuchExperimentIdException extends WebApplicationException {

  public NoSuchExperimentIdException(String experimentId) {
    super("PerformanceExperimentModel Id " + experimentId + " does not exist.");
  }
}
