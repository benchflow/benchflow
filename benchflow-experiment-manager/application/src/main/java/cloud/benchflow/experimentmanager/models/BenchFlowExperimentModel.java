package cloud.benchflow.experimentmanager.models;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.TreeMap;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.utils.IndexType;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-03-23
 */
@Entity
@Indexes({@Index(options = @IndexOptions(),
    fields = {@Field(value = "hashedID", type = IndexType.HASHED)})})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BenchFlowExperimentModel {

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
  private BenchFlowExperimentState state;
  private RunningState runningState;
  private TerminatedState terminatedState;
  private FailureStatus failureStatus = null;
  private int numTrials;
  // TODO - this should be part of the DSL
  private int numTrialRetries = 1;
  @Reference
  private TreeMap<Long, TrialModel> trials = new TreeMap<>();

  private String driverMakerExperimentBundle;

  BenchFlowExperimentModel() {
    // Empty constructor for MongoDB + Morphia
  }

  public BenchFlowExperimentModel(String experimentID) {

    this.id = experimentID;

    this.hashedID = this.id;
    this.state = BenchFlowExperimentState.START;
    this.runningState = RunningState.DETERMINE_EXECUTE_TRIALS;

    DriversMakerCompatibleID compatibleID = new DriversMakerCompatibleID(id);

    this.driverMakerExperimentBundle =
        BenchFlowExperimentManagerApplication.getMinioServiceAddress() + "/minio/"
            + BenchFlowConstants.TESTS_BUCKET + "/" + compatibleID.getMinioID();

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

  public BenchFlowExperimentState getState() {
    return state;
  }

  public void setState(BenchFlowExperimentState state) {
    this.state = state;
  }

  public RunningState getRunningState() {
    return runningState;
  }

  public void setRunningState(RunningState runningState) {
    this.runningState = runningState;
  }

  public TerminatedState getTerminatedState() {
    return terminatedState;
  }

  public void setTerminatedState(TerminatedState terminatedState) {
    this.terminatedState = terminatedState;
  }

  public FailureStatus getFailureStatus() {
    return failureStatus;
  }

  public void setFailureStatus(FailureStatus failureStatus) {
    this.failureStatus = failureStatus;
  }

  public int getNumTrials() {
    return numTrials;
  }

  public void setNumTrials(int numTrials) {
    this.numTrials = numTrials;
  }

  public int getNumTrialRetries() {
    return numTrialRetries;
  }

  public void addTrial(long index, TrialModel trialModel) {

    trials.put(index, trialModel);
  }

  public TreeMap<Long, TrialModel> getTrials() {
    return trials;
  }

  public String getDriverMakerExperimentBundle() {
    return driverMakerExperimentBundle;
  }

  @JsonIgnore
  public long getNumExecutedTrials() {

    if (trials.size() == 0) {
      return 0;
    }

    // return the greatest key
    return trials.lastEntry().getKey();
  }

  @JsonIgnore
  public String getLastExecutedTrialID() {

    // assumes that trials have been inserted in order (highest key is last)

    return trials.lastEntry().getValue().getId();
  }

  public enum BenchFlowExperimentState {
    START, READY, RUNNING, TERMINATED
  }

  public enum RunningState {
    DETERMINE_EXECUTE_TRIALS, HANDLE_TRIAL_RESULT, CHECK_TERMINATION_CRITERIA
  }

  public enum TerminatedState {
    COMPLETED, FAILURE, ABORTED, ERROR
  }

  public enum FailureStatus {
    SUT, LOAD, EXECUTION, SEVERE
  }
}
