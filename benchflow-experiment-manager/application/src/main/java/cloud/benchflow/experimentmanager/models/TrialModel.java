package cloud.benchflow.experimentmanager.models;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.faban.client.responses.RunInfo;
import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
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
  @JsonIgnoreProperties(ignoreUnknown = true)
  private String hashedID;
  private String fabanRunID;
  private Date start = new Date();
  private Date lastModified = new Date();
  private RunStatus.StatusCode fabanStatus;
  private RunInfo.Result fabanResult;
  private TrialStatus trialStatus;
  private int numRetries = 0;
  private String fabanRunStatus;

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

  public String getHashedId() {
    return hashedID;
  }

  public Date getStart() {
    return start;
  }

  public Date getLastModified() {
    return lastModified;
  }


  public RunStatus.StatusCode getFabanStatus() {
    return fabanStatus;
  }

  public void setFabanStatus(RunStatus.StatusCode fabanStatus) {
    this.fabanStatus = fabanStatus;
  }

  public Result getFabanResult() {
    return fabanResult;
  }

  public void setFabanResult(Result fabanResult) {
    this.fabanResult = fabanResult;
  }

  public TrialStatus getTrialStatus() {
    return trialStatus;
  }

  public void setTrialStatus(TrialStatus trialStatus) {
    this.trialStatus = trialStatus;
  }

  public String getFabanRunID() {
    return fabanRunID;
  }

  public void setFabanRunID(String fabanRunID) {

    this.fabanRunID = fabanRunID;
    this.fabanRunStatus = BenchFlowExperimentManagerApplication.getFabanManagerServiceAddress()
        + "/resultframe.jsp?runId=" + fabanRunID + "&result=summary.xml&show=logs";

  }

  public String getFabanRunStatus() {
    return fabanRunStatus;
  }

  public int getNumRetries() {
    return numRetries;
  }

  public void incrementRetries() {
    numRetries++;
  }

  public enum TrialStatus {
    SUCCESS, FAILED, RANDOM_FAILURE
  }
}
