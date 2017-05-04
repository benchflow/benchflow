package cloud.benchflow.testmanager.configurations.factory;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 18.02.17.
 */
public class BenchFlowEnvironmentFactory {

  @NotEmpty
  private String configPath;

  @JsonProperty("config.yml")
  public String getConfigPath() {
    return configPath;
  }

  @JsonProperty("config.yml")
  public void setConfigPath(String configPath) {
    this.configPath = configPath;
  }
}
