package cloud.benchflow.testmanager.models.explorationspace;

import cloud.benchflow.dsl.definition.types.bytes.Bytes;
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpace;
import cloud.benchflow.testmanager.api.response.ExplorationSpacePointResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-03
 */
public class MongoCompatibleExplorationSpace {

  // we use concrete collection classes for MongoDB + Morphia
  // need to ensure that Optional are not null when read from DB => initialize to empty

  private int size;
  private Optional<List<Integer>> usersDimension = Optional.empty();
  private Optional<Map<String, List<Bytes>>> memoryDimension = Optional.empty();
  private Optional<Map<String, Map<String, List<String>>>> environmentDimension = Optional.empty();

  public MongoCompatibleExplorationSpace() {
    // Empty constructor for MongoDB + Morphia
  }

  public MongoCompatibleExplorationSpace(JavaCompatExplorationSpace javaCompatExplorationSpace) {
    this(javaCompatExplorationSpace.size(), javaCompatExplorationSpace.usersDimension(),
        javaCompatExplorationSpace.memoryDimension(), javaCompatExplorationSpace.environment());
  }

  public MongoCompatibleExplorationSpace(int size, Optional<List<Integer>> usersDimension,
      Optional<Map<String, List<Bytes>>> memoryDimension,
      Optional<Map<String, Map<String, List<String>>>> environmentDimension) {
    this.size = size;

    this.usersDimension = usersDimension.map(ArrayList::new);

    this.memoryDimension = memoryDimension.map(map -> new HashMap<>(map.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())))));

    this.environmentDimension =
        environmentDimension.map(map -> new HashMap<>(map.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                e -> new HashMap<>(e.getValue().entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, e1 -> new ArrayList<>(e1.getValue()))))))));
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public Optional<List<Integer>> getUsersDimension() {
    return usersDimension;
  }

  public void setUsersDimension(Optional<List<Integer>> usersDimension) {
    this.usersDimension = usersDimension;
  }

  public Optional<Map<String, List<Bytes>>> getMemoryDimension() {
    return memoryDimension;
  }

  public void setMemoryDimension(Optional<Map<String, List<Bytes>>> memoryDimension) {
    this.memoryDimension = memoryDimension;
  }

  public Optional<Map<String, Map<String, List<String>>>> getEnvironmentDimension() {
    return environmentDimension;
  }

  public void setEnvironmentDimension(
      Optional<Map<String, Map<String, List<String>>>> environmentDimension) {
    this.environmentDimension = environmentDimension;
  }

  public JavaCompatExplorationSpace toJavaCompat() {

    return new JavaCompatExplorationSpace(size,

        usersDimension.map(ArrayList::new),

        memoryDimension.map(map -> new HashMap<>(map.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue()))))),

        environmentDimension.map(map -> new HashMap<>(map.entrySet().stream().collect(Collectors
            .toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e1 -> new ArrayList<>(e1.getValue())))))))));
  }

  public ExplorationSpacePointResponse getExplorationSpacePointResponse(int index) {

    ExplorationSpacePointResponse explorationSpacePointResponse =
        new ExplorationSpacePointResponse();

    if (index >= size || index < 0) {
      return explorationSpacePointResponse;
    }

    usersDimension
        .ifPresent(integers -> explorationSpacePointResponse.setUsers(integers.get(index)));

    memoryDimension.ifPresent(memoryDimension -> explorationSpacePointResponse
        .setMemory(memoryDimension.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(index).toString()))));

    environmentDimension.ifPresent(environmentDimension -> explorationSpacePointResponse
        .setEnvironment(environmentDimension.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e1 -> e1.getValue().get(index)))))));

    return explorationSpacePointResponse;

  }

}
