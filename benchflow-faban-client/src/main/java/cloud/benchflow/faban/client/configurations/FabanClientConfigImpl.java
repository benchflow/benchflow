package cloud.benchflow.faban.client.configurations;

import java.net.URI;

/**
 * @author Simone D'Avico <simonedavico@gmail.com>
 *
 * A configuration class for the faban client.
 * The user can configure username and password,
 * or username, password and masterURL.
 *
 */
public class FabanClientConfigImpl implements FabanClientConfig {

  private final String user;
  private final String password;
  private final URI masterURL;

  public FabanClientConfigImpl(final String user, final String password) {
    this(user, password, new FabanClientDefaultConfig().getMasterURL());
  }

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
