package cloud.benchflow.dsl.explorationspace.javatypes;

import cloud.benchflow.dsl.definition.configuration.goal.explorationspace.explorationvalues.ByteValues;
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

  public Optional<Map<String, List<Bytes>>> getMemory() {
    return memory;
  }

  public Optional<Map<String, Map<String, List<String>>>> getEnvironment() {
    return environment;
  }
}
