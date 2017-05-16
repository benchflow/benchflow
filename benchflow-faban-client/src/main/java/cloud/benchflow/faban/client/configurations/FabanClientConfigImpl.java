package cloud.benchflow.faban.client.configurations;

import java.net.URI;

/**
 * A configuration class for the faban client.
 * The user can configure username and password,
 * or username, password and masterURL.
 *
 * @author Simone D'Avico (simonedavico@gmail.com)
 */
public class FabanClientConfigImpl implements FabanClientConfig {

  private final String user;
  private final String password;
  private final URI masterURL;

  /**
   * Creates a FabanClientConfigImpl on the default url.
   * @param user Faban harness username
   * @param password Faban harness password
   */
  public FabanClientConfigImpl(final String user, final String password) {
    this(user, password, new FabanClientDefaultConfig().getMasterURL());
  }

  /**
   * Creates a FabanClientConfigImpl on a custom url.
   * @param user Faban harness username
   * @param password Faban harness password
   * @param masterURL Faban harness url
   */
  public FabanClientConfigImpl(final String user, final String password, final URI masterURL) {
    this.user = user;
    this.password = password;
    this.masterURL = masterURL;
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
}
