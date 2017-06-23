package cloud.benchflow.faban.client.exceptions;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 28/10/15.
 * @author vincenzoferme
 *
 *         Exception so that the client has to decide how to handle the case, according to its
 *         business logic
 */
public class ConfigFileNotFoundException extends FabanClientException {

  public ConfigFileNotFoundException(String message) {
    super(message);
  }
}
