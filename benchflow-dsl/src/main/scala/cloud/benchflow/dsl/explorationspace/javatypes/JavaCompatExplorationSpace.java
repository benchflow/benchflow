package cloud.benchflow.dsl.explorationspace.javatypes;

import cloud.benchflow.dsl.definition.types.bytes.Bytes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com)
 *         created on 2017-06-05
 */
public class JavaCompatExplorationSpace {

  private int size;
  private Optional<List<Integer>> usersDimension;
  private Optional<Map<String, List<Bytes>>> memoryDimension;
  private Optional<Map<String, Map<String, List<String>>>> environmentDimension;

  public JavaCompatExplorationSpace() {
    // Empty constructor for MongoDB + Morphia
  }

  public JavaCompatExplorationSpace(int size, Optional<List<Integer>> usersDimension, Optional<Map<String, List<Bytes>>> memoryDimension, Optional<Map<String, Map<String, List<String>>>> environmentDimension) {
    this.size = size;
    this.usersDimension = usersDimension;
    this.memoryDimension = memoryDimension;
    this.environmentDimension = environmentDimension;
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

  public void setEnvironmentDimension(Optional<Map<String, Map<String, List<String>>>> environmentDimension) {
    this.environmentDimension = environmentDimension;
  }
}
