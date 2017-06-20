package cloud.benchflow.faban.client.configurations;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by simonedavico on 26/10/15.
 * <p>
 * <p>Default configuration for the Faban client.
 *
 * @author vincenzoferme
 */
public class FabanClientDefaultConfig implements FabanClientConfig {

  private FabanClientConfigImpl defaultConfig;

  /**
   * Default constructor for the FabanClientDefaultConfig.
   */
  public FabanClientDefaultConfig() {

    FabanClientConfigImpl defConf = null;
    try {
      defConf =
          new FabanClientConfigImpl("deployer", "adminadmin", new URI("http://localhost:9980"));
    } catch (URISyntaxException e) {
      System.err.println("There was a problem initializing the default "
          + "faban client configuration. See the stack" + "trace for more informations.");
      e.printStackTrace();
    }
    this.defaultConfig = defConf;
  }

  public String getUser() {
    return defaultConfig.getUser();
  }

  public String getPassword() {
    return defaultConfig.getPassword();
  }

  public URI getMasterURL() {
    return defaultConfig.getMasterURL();
  }

  public URI getControllerURL() {
    return defaultConfig.getControllerURL();
  }

}
