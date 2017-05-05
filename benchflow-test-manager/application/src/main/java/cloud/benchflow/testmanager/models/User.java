package cloud.benchflow.testmanager.models;

import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.util.HashSet;
import java.util.Set;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 21.02.17. */
@Entity
@Indexes({
  @Index(
    options = @IndexOptions(),
    fields = {@Field(value = "hashUsername", type = IndexType.HASHED)}
  )
})
public class User {

  public static String ID_FIELD_NAME = "username";
  public static String HASHED_ID_FIELD_NAME = "hashUsername";

  @Id private String username;

  private String hashUsername;

  @Reference private Set<BenchFlowTestModel> testModels = new HashSet<>();

  public User() {
    // Empty constructor for MongoDB + Morphia
  }

  public User(String username) {

    this.username = username;
    this.hashUsername = username;
  }

  public String getUsername() {
    return username;
  }

  public boolean addTestModel(BenchFlowTestModel testModel) {

    return testModels.add(testModel);
  }

  public boolean removeTestModel(BenchFlowTestModel testModel) {

    return testModels.remove(testModel);
  }

  public void removeAllTestModels() {
    testModels.clear();
  }

  public Set<BenchFlowTestModel> getTestModels() {
    return testModels;
  }
}
