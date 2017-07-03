package cloud.benchflow.dsl.explorationspace.javatypes;

import cloud.benchflow.dsl.definition.types.bytes.Bytes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-05
 */
public class JavaCompatExplorationSpacePoint {

  // we use concrete collection classes for MongoDB + Morphia

  private Optional<Integer> users;
  private Optional<HashMap<String, Bytes>> memory;
  private Optional<HashMap<String, HashMap<String, String>>> environment;

  public JavaCompatExplorationSpacePoint() {
    // Empty constructor for MongoDB + Morphia
  }

  public JavaCompatExplorationSpacePoint(Optional<Integer> users, Optional<Map<String, Bytes>> memory, Optional<Map<String, Map<String, String>>> environment) {
    this.users = users;
    this.memory = memory.map(map -> new HashMap<>(map.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue
    ))));

    this.environment = environment.map(map -> new HashMap<>(map.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> new HashMap<>(
            e.getValue().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ))
        )
    ))));
  }

  public Optional<Integer> getUsers() {
    return users;
  }

  public void setUsers(Optional<Integer> users) {
    this.users = users;
  }

  public Optional<HashMap<String, Bytes>> getMemory() {
    return memory;
  }

  public void setMemory(Optional<HashMap<String, Bytes>> memory) {
    this.memory = memory;
  }

  public Optional<HashMap<String, HashMap<String, String>>> getEnvironment() {
    return environment;
  }

  public void setEnvironment(Optional<HashMap<String, HashMap<String, String>>> environment) {
    this.environment = environment;
  }
}
