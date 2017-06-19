package cloud.benchflow.testmanager.models;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

import cloud.benchflow.faban.client.responses.RunStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.utils.IndexType;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 18.12.16.
 */
@Entity
@Indexes({@Index(options = @IndexOptions(),
    fields = {@Field(value = "hashedID", type = IndexType.HASHED)})})
public class BenchFlowExperimentModel {

  public static final String ID_FIELD_NAME = "id";
  public static final String HASHED_ID_FIELD_NAME = "hashedID";
  @Id
  private String id;
  // used for potential sharding in the future
  @JsonIgnore
  private String hashedID;
  @JsonIgnore
  private String testID;
  @JsonIgnore
  private long number;
  private int explorationPointIndex;
  private Date start = new Date();
  private Date lastModified = new Date();
  private BenchFlowExperimentState state;
  private RunningState runningState;
  private TerminatedState terminatedState;
  private Map<Long, RunStatus.Code> trials = new HashMap<>();

  BenchFlowExperimentModel() {
    // Empty constructor for MongoDB + Morphia
  }

  public BenchFlowExperimentModel(String testID, long experimentNumber) {

    this.testID = testID;
    this.number = experimentNumber;

    this.id = testID + MODEL_ID_DELIMITER + experimentNumber;

    this.hashedID = this.id;
    this.state = BenchFlowExperimentState.START;
  }

  @PrePersist
  void prePersist() {
    lastModified = new Date();
  }

  public String getId() {
    return id;
  }

  public long getNumber() {
    return number;
  }

  public int getExplorationPointIndex() {
    return explorationPointIndex;
  }

  public void setExplorationPointIndex(int explorationPointIndex) {
    this.explorationPointIndex = explorationPointIndex;
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

  public Map<Long, RunStatus.Code> getTrials() {
    return trials;
  }

  public void setTrialStatus(long trialNumber, RunStatus.Code status) {

    trials.put(trialNumber, status);
  }

  public RunStatus.Code getTrialStatus(long trialNumber) {

    return trials.get(trialNumber);
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
}
