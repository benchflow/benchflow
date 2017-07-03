package cloud.benchflow.dsl.explorationspace.javatypes;

import cloud.benchflow.dsl.definition.types.bytes.Bytes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-05
 */
public class JavaCompatExplorationSpace {

  // we use concrete collection classes for MongoDB + Morphia

  private int size;
  private Optional<ArrayList<Integer>> usersDimension;
  private Optional<HashMap<String, ArrayList<Bytes>>> memoryDimension;
  private Optional<HashMap<String, HashMap<String, ArrayList<String>>>> environmentDimension;

  public JavaCompatExplorationSpace() {
    // Empty constructor for MongoDB + Morphia
  }

  public JavaCompatExplorationSpace(int size, Optional<List<Integer>> usersDimension, Optional<Map<String, List<Bytes>>> memoryDimension, Optional<Map<String, Map<String, List<String>>>> environmentDimension) {
    this.size = size;

    this.usersDimension = usersDimension.map(ArrayList::new);

    this.memoryDimension = memoryDimension.map(map -> new HashMap<>(map.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> new ArrayList<>(e.getValue())
    ))));

    this.environmentDimension = environmentDimension.map(map -> new HashMap<>(map.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> new HashMap<>(
            e.getValue().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e1 -> new ArrayList<>(e1.getValue())
            ))
        )
    ))));
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public Optional<ArrayList<Integer>> getUsersDimension() {
    return usersDimension;
  }

  public void setUsersDimension(Optional<ArrayList<Integer>> usersDimension) {
    this.usersDimension = usersDimension;
  }

  public Optional<HashMap<String, ArrayList<Bytes>>> getMemoryDimension() {
    return memoryDimension;
  }

  public void setMemoryDimension(Optional<HashMap<String, ArrayList<Bytes>>> memoryDimension) {
    this.memoryDimension = memoryDimension;
  }

  public Optional<HashMap<String, HashMap<String, ArrayList<String>>>> getEnvironmentDimension() {
    return environmentDimension;
  }

  public void setEnvironmentDimension(Optional<HashMap<String, HashMap<String, ArrayList<String>>>> environmentDimension) {
    this.environmentDimension = environmentDimension;
  }
}
