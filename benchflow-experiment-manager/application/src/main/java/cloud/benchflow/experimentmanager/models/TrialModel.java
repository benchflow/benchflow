package cloud.benchflow.experimentmanager.models;

import cloud.benchflow.faban.client.responses.RunStatus;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.util.Date;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-03-23 */
@Entity
@Indexes({
  @Index(
    options = @IndexOptions(),
    fields = {@Field(value = "hashedID", type = IndexType.HASHED)}
  )
})
public class TrialModel {

  public static final String ID_FIELD_NAME = "id";
  public static final String HASHED_ID_FIELD_NAME = "hashedID";
  @Id private String id;
  // used for potential sharding in the future
  private String hashedID;
  private String fabanRunID;
  private Date start = new Date();
  private Date lastModified = new Date();
  private RunStatus.Code status;
  private int numRetries = 0;

  public TrialModel() {
    // Empty constructor for MongoDB + Morphia
  }

  public TrialModel(String trialID) {
    this.id = trialID;
  }

  @PrePersist
  void prePersist() {
    lastModified = new Date();
  }

  public String getId() {
    return id;
  }

  public Date getStart() {
    return start;
  }

  public Date getLastModified() {
    return lastModified;
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
}
