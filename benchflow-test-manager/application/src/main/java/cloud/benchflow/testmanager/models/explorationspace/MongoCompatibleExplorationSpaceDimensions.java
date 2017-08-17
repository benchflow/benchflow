package cloud.benchflow.testmanager.models.explorationspace;

import cloud.benchflow.dsl.definition.types.bytes.Bytes;
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpaceDimensions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-03
 */
public class MongoCompatibleExplorationSpaceDimensions {

  // we use concrete collection classes for MongoDB + Morphia

  private Optional<List<Integer>> users;
  private Optional<Map<String, List<String>>> memory;
  private Optional<Map<String, Map<String, List<String>>>> environment;

  public MongoCompatibleExplorationSpaceDimensions() {
    // Empty constructor for MongoDB + Morphia
  }

  public MongoCompatibleExplorationSpaceDimensions(
      JavaCompatExplorationSpaceDimensions javaCompatExplorationSpaceDimensions) {
    this(javaCompatExplorationSpaceDimensions.users(),
        javaCompatExplorationSpaceDimensions.memory(),
        javaCompatExplorationSpaceDimensions.environment());
  }

  public MongoCompatibleExplorationSpaceDimensions(Optional<List<Integer>> users,
      Optional<Map<String, List<Bytes>>> memory,
      Optional<Map<String, Map<String, List<String>>>> environment) {
    this.users = users.map(ArrayList::new);

    this.memory = memory.map(
        map -> new HashMap<>(map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
            e -> e.getValue().stream().map(Bytes::toString).collect(Collectors.toList())))));

    this.environment = environment.map(map -> new HashMap<>(map.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey,
            e -> new HashMap<>(e.getValue().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e1 -> new ArrayList<>(e1.getValue()))))))));
  }

  public Optional<List<Integer>> getUsers() {
    return users;
  }

  public void setUsers(Optional<List<Integer>> users) {
    this.users = users;
  }

  public Optional<Map<String, List<String>>> getMemory() {
    return memory;
  }

  public void setMemory(Optional<Map<String, List<String>>> memory) {
    this.memory = memory;
  }

  public Optional<Map<String, Map<String, List<String>>>> getEnvironment() {
    return environment;
  }

  public void setEnvironment(Optional<Map<String, Map<String, List<String>>>> environment) {
    this.environment = environment;
  }

  public JavaCompatExplorationSpaceDimensions toJavaCompat() {

    return new JavaCompatExplorationSpaceDimensions(users,
        memory
            .map(map -> map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
                    .map(s -> Bytes.fromString(s).get()).collect(Collectors.toList())))),
        environment);
  }

}
