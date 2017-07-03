package cloud.benchflow.dsl.explorationspace.javatypes;

import cloud.benchflow.dsl.definition.types.bytes.Bytes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-05
 */
public class JavaCompatExplorationSpaceDimensions {

  // we use concrete collection classes for MongoDB + Morphia

  private Optional<ArrayList<Integer>> users;
  private Optional<HashMap<String, ArrayList<Bytes>>> memory;
  private Optional<HashMap<String, HashMap<String, ArrayList<String>>>> environment;

  public JavaCompatExplorationSpaceDimensions() {
    // Empty constructor for MongoDB + Morphia
  }

  public JavaCompatExplorationSpaceDimensions(Optional<List<Integer>> users, Optional<Map<String, List<Bytes>>> memory, Optional<Map<String, Map<String, List<String>>>> environment) {
    this.users = users.map(ArrayList::new);

    this.memory = memory.map(map -> new HashMap<>(map.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> new ArrayList<>(e.getValue())
    ))));

    this.environment = environment.map(map -> new HashMap<>(map.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> new HashMap<>(
            e.getValue().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e1 -> new ArrayList<>(e1.getValue())
            ))
        )
    ))));
  }

  public Optional<ArrayList<Integer>> getUsers() {
    return users;
  }

  public void setUsers(Optional<ArrayList<Integer>> users) {
    this.users = users;
  }

  public Optional<HashMap<String, ArrayList<Bytes>>> getMemory() {
    return memory;
  }

  public void setMemory(Optional<HashMap<String, ArrayList<Bytes>>> memory) {
    this.memory = memory;
  }

  public Optional<HashMap<String, HashMap<String, ArrayList<String>>>> getEnvironment() {
    return environment;
  }

  public void setEnvironment(Optional<HashMap<String, HashMap<String, ArrayList<String>>>> environment) {
    this.environment = environment;
  }
}
