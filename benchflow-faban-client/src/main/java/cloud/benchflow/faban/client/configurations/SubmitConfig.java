package cloud.benchflow.faban.client.configurations;

import java.io.InputStream;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 28/10/15.
 */
public class SubmitConfig implements Config {

  private String benchmarkName;
  private String profile;
  private InputStream configFile;

  /**
   * Construct a SubmitConfig.
   *
   * @param benchmarkName the benchmark name
   * @param profile the benchmark profile
   * @param configFile the configuration file for this run
   */
  public SubmitConfig(String benchmarkName, String profile, InputStream configFile) {
    this.benchmarkName = benchmarkName;
    this.profile = profile;
    this.configFile = configFile;
  }

  public String getBenchmarkName() {
    return benchmarkName;
  }

  public String getProfile() {
    return profile;
  }

  public InputStream getConfigFile() {
    return configFile;
  }
}
