package cloud.benchflow.experimentmanager.models;

import static cloud.benchflow.experimentmanager.models.TrialModel.HandleTrialResultState.CHECK_TRIAL_RESULT;

import cloud.benchflow.faban.client.responses.RunStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.utils.IndexType;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-03-23
 */
@Entity
@Indexes({@Index(options = @IndexOptions(),
    fields = {@Field(value = "hashedID", type = IndexType.HASHED)})})
public class TrialModel {

  /**
   * NOTE: This class is also annotated with Jackson annotation since we then easily can return it
   * when the user asks for the status of a given test. This annotation is not needed to store in
   * MongoDB.
   */

  public static final String ID_FIELD_NAME = "id";
  public static final String HASHED_ID_FIELD_NAME = "hashedID";
  @Id
  private String id;
  // used for potential sharding in the future
  @JsonIgnore
  private String hashedID;
  private String fabanRunID;
  private Date start = new Date();
  private Date lastModified = new Date();
  private HandleTrialResultState handleTrialResultState;
  private RunStatus.Code status;
  private int numRetries = 0;

  public TrialModel() {
    // Empty constructor for MongoDB + Morphia
  }

  public TrialModel(String trialID) {
    this.id = trialID;
    this.handleTrialResultState = CHECK_TRIAL_RESULT;
  }

  @PrePersist
  void prePersist() {
    lastModified = new Date();
  }

  public String getId() {
    return id;
  }

  public String getHashedId() {
    return hashedID;
  }

  public Date getStart() {
    return start;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public HandleTrialResultState getHandleTrialResultState() {
    return handleTrialResultState;
  }

  public void setHandleTrialResultState(HandleTrialResultState handleTrialResultState) {
    this.handleTrialResultState = handleTrialResultState;
  }

  public RunStatus.Code getStatus() {
    return status;
  }

  public void setStatus(RunStatus.Code status) {
    this.status = status;
  }

  public String getFabanRunID() {
    return fabanRunID;
  }

  public void setFabanRunID(String fabanRunID) {
    this.fabanRunID = fabanRunID;
  }

  public int getNumRetries() {
    return numRetries;
  }

  public void incrementRetries() {
    numRetries++;
  }

  public enum HandleTrialResultState {
    CHECK_TRIAL_RESULT, RE_EXECUTE_TRIAL
  }
}
