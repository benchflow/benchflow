package cloud.benchflow.dsl.explorationspace.javatypes;

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues.ByteValues;
import cloud.benchflow.dsl.definition.types.bytes.Bytes;

import java.util.Map;
import java.util.Optional;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-05
 */
public class JavaCompatExplorationSpacePoint {

  private Optional<Integer> users;
  private Optional<Map<String, Bytes>> memory;
  private Optional<Map<String, Map<String, String>>> environment;

  public JavaCompatExplorationSpacePoint(Optional<Integer> users, Optional<Map<String, Bytes>> memory, Optional<Map<String, Map<String, String>>> environment) {
    this.users = users;
    this.memory = memory;
    this.environment = environment;
  }

  public Optional<Integer> getUsers() {
    return users;
  }

  public void setUsers(Optional<Integer> users) {
    this.users = users;
  }

  public Optional<Map<String, Bytes>> getMemory() {
    return memory;
  }

  public void setMemory(Optional<Map<String, Bytes>> memory) {
    this.memory = memory;
  }

  public Optional<Map<String, Map<String, String>>> getEnvironment() {
    return environment;
  }

  public void setEnvironment(Optional<Map<String, Map<String, String>>> environment) {
    this.environment = environment;
  }
}
