package cloud.benchflow.experimentmanager.models;

import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-03-23 */
@Entity
@Indexes({
  @Index(
    options = @IndexOptions(),
    fields = {@Field(value = "hashedID", type = IndexType.HASHED)}
  )
})
public class BenchFlowExperimentModel {

  public static final String ID_FIELD_NAME = "id";
  public static final String HASHED_ID_FIELD_NAME = "hashedID";
  @Id private String id;
  // used for potential sharding in the future
  private String hashedID;
  private Date start = new Date();
  private Date lastModified = new Date();
  private BenchFlowExperimentState state;
  private RunningState runningState;
  private TerminatedState terminatedState;
  @Reference private List<TrialModel> trials = new ArrayList<>();

  BenchFlowExperimentModel() {
    // Empty constructor for MongoDB + Morphia
  }

  public BenchFlowExperimentModel(String experimentID) {

    this.id = experimentID;

    this.hashedID = this.id;
    this.state = BenchFlowExperimentState.START;
    this.runningState = RunningState.EXECUTE_NEW_TRIAL;
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

  public void addTrial(TrialModel trialModel) {

    trials.add(trialModel);
  }

  public enum BenchFlowExperimentState {
    START,
    READY,
    RUNNING,
    TERMINATED
  }

  public enum RunningState {
    EXECUTE_NEW_TRIAL,
    HANDLE_TRIAL_RESULT,
    CHECK_TERMINATION_CRITERIA,
    RE_EXECUTE_TRIAL
  }

  public enum TerminatedState {
    COMPLETED,
    FAILURE,
    ABORTED,
    ERROR
  }
}
