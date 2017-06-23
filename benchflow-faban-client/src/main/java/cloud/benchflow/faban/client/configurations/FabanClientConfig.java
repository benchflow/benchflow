package cloud.benchflow.faban.client.configurations;

import java.net.URI;

/**
 * Configuration class for the Faban Client.
 *
 * @author vincenzoferme
 */
public interface FabanClientConfig extends Config {

  String getUser();

  String getPassword();

  URI getMasterURL();

  URI getControllerURL();
}
