package cloud.benchflow.experimentmanager.models;

import cloud.benchflow.faban.client.responses.RunInfo.Result;
import cloud.benchflow.faban.client.responses.RunStatus.StatusCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
  private Date start = new Date();
  private Date lastModified = new Date();
  private TrialStatus trialStatus;
  private int numRetries = 0;
  private List<FabanInfo> fabanInfoList = new ArrayList<>();

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


  public TrialStatus getTrialStatus() {
    return trialStatus;
  }

  public void setTrialStatus(TrialStatus trialStatus) {
    this.trialStatus = trialStatus;
  }

  public int getNumRetries() {
    return numRetries;
  }

  public void incrementRetries() {
    numRetries++;
  }

  public String getHashedID() {
    return hashedID;
  }

  public void setHashedID(String hashedID) {
    this.hashedID = hashedID;
  }

  public List<FabanInfo> getFabanInfoList() {
    return fabanInfoList;
  }

  public void setFabanInfoList(List<FabanInfo> fabanInfoList) {
    this.fabanInfoList = fabanInfoList;
  }

  @JsonIgnore
  public void setFabanRunID(String fabanRunID) {

    if (fabanInfoList.size() <= numRetries) {
      fabanInfoList.add(new FabanInfo());
    }

    fabanInfoList.get(numRetries).setFabanRunID(fabanRunID);

  }

  @JsonIgnore
  public String getFabanRunID() {

    if (numRetries < fabanInfoList.size()) {
      return fabanInfoList.get(numRetries).getFabanRunID();
    }

    return null;

  }

  @JsonIgnore
  public void setFabanStatus(StatusCode fabanStatus) {

    if (fabanInfoList.size() <= numRetries) {
      fabanInfoList.add(new FabanInfo());
    }

    fabanInfoList.get(numRetries).setFabanStatus(fabanStatus);

  }

  @JsonIgnore
  public StatusCode getFabanStatus() {

    if (numRetries < fabanInfoList.size()) {
      return fabanInfoList.get(numRetries).getFabanStatus();
    }

    return null;
  }

  @JsonIgnore
  public void setFabanResult(Result fabanResult) {

    if (fabanInfoList.size() <= numRetries) {
      fabanInfoList.add(new FabanInfo());
    }

    fabanInfoList.get(numRetries).setFabanResult(fabanResult);

  }

  @JsonIgnore
  public Result getFabanResult() {

    if (numRetries < fabanInfoList.size()) {
      return fabanInfoList.get(numRetries).getFabanResult();
    }

    return null;
  }

  @JsonIgnore
  public String getFabanRunStatusURI() {
    if (numRetries < fabanInfoList.size()) {
      return fabanInfoList.get(numRetries).getFabanRunStatusURI();
    }

    return null;
  }

  public enum TrialStatus {
    SUCCESS, FAILED, RANDOM_FAILURE
  }
}
