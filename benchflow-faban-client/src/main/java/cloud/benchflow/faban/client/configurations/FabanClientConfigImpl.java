package cloud.benchflow.faban.client.configurations;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A configuration class for the faban client.
 * The user can configure username and password,
 * or username, password and masterURL.
 *
 * @author Simone D'Avico (simonedavico@gmail.com)\
 * @author vincenzoferme
 */
public class FabanClientConfigImpl implements FabanClientConfig {

  private final String user;
  private final String password;
  private final URI masterURL;
  private final URI controllerURL;

  /**
   * Creates a FabanClientConfigImpl on the default url.
   *
   * @param user Faban harness username
   * @param password Faban harness password
   */
  public FabanClientConfigImpl(final String user, final String password) {
    this(user, password, new FabanClientDefaultConfig().getMasterURL());
  }

  /**
   * Creates a FabanClientConfigImpl on a custom url.
   *
   * @param user Faban harness username
   * @param password Faban harness password
   * @param masterURL Faban harness url
   */
  public FabanClientConfigImpl(final String user, final String password, final URI masterURL) {
    this.user = user;
    this.password = password;
    this.masterURL = masterURL;

    URI controllerURL = null;

    try {
      controllerURL = new URI(masterURL + "/controller");
    } catch (URISyntaxException e) {
      System.err.println("There was a problem initializing the default "
          + "faban client configuration. See the stack" + "trace for more informations.");
      e.printStackTrace();
    }

    this.controllerURL = controllerURL;

  }


  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public URI getMasterURL() {
    return masterURL;
  }

  public URI getControllerURL() {
    return controllerURL;
  }
}
