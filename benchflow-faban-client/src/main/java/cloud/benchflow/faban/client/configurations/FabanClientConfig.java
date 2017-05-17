package cloud.benchflow.faban.client.configurations;

import java.net.URI;

/**
 * Created by simonedavico on 26/10/15.
 */
public interface FabanClientConfig extends Config {

  String getUser();

  String getPassword();

  URI getMasterURL();
}
