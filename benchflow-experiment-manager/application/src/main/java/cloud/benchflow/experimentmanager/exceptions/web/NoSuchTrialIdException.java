package cloud.benchflow.experimentmanager.exceptions.web;

import javax.ws.rs.WebApplicationException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 06/04/16.
 */
public class NoSuchTrialIdException extends WebApplicationException {

  public NoSuchTrialIdException(String trialId) {
    super("TrialModelHibernate Id " + trialId + " does not exist.");
  }
}
