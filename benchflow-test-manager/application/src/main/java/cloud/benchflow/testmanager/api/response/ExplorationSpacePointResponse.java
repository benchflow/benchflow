package cloud.benchflow.testmanager.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-05
 */
public class ExplorationSpacePointResponse {

  @JsonProperty
  private Integer users;
  @JsonProperty
  private Map<String, String> memory;
  @JsonProperty
  private Map<String, Map<String, String>> environment;

  public Integer getUsers() {
    return users;
  }

  public void setUsers(Integer users) {
    this.users = users;
  }

  public Map<String, String> getMemory() {
    return memory;
  }

  public void setMemory(Map<String, String> memory) {
    this.memory = memory;
  }

  public Map<String, Map<String, String>> getEnvironment() {
    return environment;
  }

  public void setEnvironment(Map<String, Map<String, String>> environment) {
    this.environment = environment;
  }
}
