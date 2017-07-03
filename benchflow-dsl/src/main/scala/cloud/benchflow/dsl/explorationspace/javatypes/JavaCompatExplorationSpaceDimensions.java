package cloud.benchflow.dsl.explorationspace.javatypes;

import cloud.benchflow.dsl.definition.types.bytes.Bytes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-05
 */
public class JavaCompatExplorationSpaceDimensions {

  private Optional<List<Integer>> users;
  private Optional<Map<String, List<Bytes>>> memory;
  private Optional<Map<String, Map<String, List<String>>>> environment;

  public JavaCompatExplorationSpaceDimensions() {
    // Empty constructor for MongoDB + Morphia
  }

  public JavaCompatExplorationSpaceDimensions(
      Optional<List<Integer>> users,
      Optional<Map<String, List<Bytes>>> memory,
      Optional<Map<String, Map<String, List<String>>>> environment
  ) {
    this.users = users;
    this.memory = memory;
    this.environment = environment;
  }

  public Optional<List<Integer>> getUsers() {
    return users;
  }

  public void setUsers(Optional<List<Integer>> users) {
    this.users = users;
  }

  public Optional<Map<String, List<Bytes>>> getMemory() {
    return memory;
  }

  public void setMemory(Optional<Map<String, List<Bytes>>> memory) {
    this.memory = memory;
  }

  public Optional<Map<String, Map<String, List<String>>>> getEnvironment() {
    return environment;
  }

  public void setEnvironment(Optional<Map<String, Map<String, List<String>>>> environment) {
    this.environment = environment;
  }
}
