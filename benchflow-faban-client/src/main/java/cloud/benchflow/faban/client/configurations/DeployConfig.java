package cloud.benchflow.faban.client.configurations;

import java.io.File;

/**
 * Created by simonedavico on 26/10/15.
 * 
 * <p>Configuration class for the deploy command
 */
public class DeployConfig implements Config {

  private File jarFile;
  //    private InputStream jarFile;
  private String driverName;
  private boolean clearConfig;

  /**
   * Construct a DeployConfig from the benchmark file and a name for the driver.
   * @param jarFile the benchmark file
   * @param driverName the driver name
   */
  public DeployConfig(File jarFile, String driverName) {
    this(jarFile, driverName, true);
  }

  /**
   * Construct a DeployConfig from the benchmark file and a name for the driver,
   * clearing the previous configuration for the benchmark.
   * @param jarFile the benchmark file
   * @param driverName the driver name
   * @param clearConfig true for clearing the previous configuration for the benchmark
   */
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
