package cloud.benchflow.testmanager.models;

import java.util.HashSet;
import java.util.Set;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.utils.IndexType;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 21.02.17.
 */
@Entity
@Indexes({@Index(options = @IndexOptions(),
    fields = {@Field(value = "hashUsername", type = IndexType.HASHED)})})
public class User {

  public static String ID_FIELD_NAME = "username";
  public static String HASHED_ID_FIELD_NAME = "hashUsername";

  @Id
  private String username;

  private String hashUsername;

  @Reference
  private Set<BenchFlowTestModel> testModels = new HashSet<>();

  public User() {
    // Empty constructor for MongoDB + Morphia
  }

  public User(String username) {

    this.username = username;
    this.hashUsername = username;
  }

  public String getHashUsername() {
    return hashUsername;
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
