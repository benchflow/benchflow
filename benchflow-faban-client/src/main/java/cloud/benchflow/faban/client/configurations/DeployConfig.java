package cloud.benchflow.faban.client.configurations;

import java.io.File;

/**
 * Created by simonedavico on 26/10/15.
 *
 * Configuration class for the deploy command
 */
public class DeployConfig implements Config {

  private File jarFile;
  //    private InputStream jarFile;
  private String driverName;
  private boolean clearConfig;

  public DeployConfig(File jarFile, String driverName) {
    this(jarFile, driverName, true);
  }

  public DeployConfig(File jarFile, String driverName, boolean clearConfig) {
    this.jarFile = jarFile;
    this.driverName = driverName;
    this.clearConfig = clearConfig;
  }

  public File getJarFile() {
    return jarFile;
  }

  public boolean clearConfig() {
    return clearConfig;
  }

  public String getDriverName() {
    return driverName;
  }
}
