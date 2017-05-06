package cloud.benchflow.testmanager.models;

import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.READY;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.START;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState.*;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 18.12.16. */
@Entity
@Indexes({
  @Index(
    options = @IndexOptions(),
    fields = {@Field(value = "hashedID", type = IndexType.HASHED)}
  )
})
public class BenchFlowTestModel {

  /**
   * NOTE: This class is also annotated with Jackson annotation since we then easily can return it
   * when the user asks for the status of a given test. This annotation is not needed to store in
   * MongoDB.
   */
  public static final String ID_FIELD_NAME = "id";

  public static final String HASHED_ID_FIELD_NAME = "hashedID";
  @Id private String id;

  // Annotations for MongoDB + Morphia (http://mongodb.github.io/morphia/1.3/guides/annotations/#entity)

  //    userName.testName.testNumber.experimentNumber.trialNumber
  // used for potential sharing in the future
  @JsonIgnore private String hashedID;
  @Reference @JsonIgnore private User user;
  @JsonIgnore private String name;
  @JsonIgnore private long number;
  private Date start = new Date();
  private Date lastModified = new Date();
  private BenchFlowTestState state;
  private TestRunningState runningState;
  private TestTerminatedState terminatedState;
  @Reference private Set<BenchFlowExperimentModel> experiments = new HashSet<>();

  @JsonIgnore private ExplorationModel explorationModel = new ExplorationModel();

  public BenchFlowTestModel() {
    // Empty constructor for MongoDB + Morphia
  }

  public BenchFlowTestModel(User user, String benchFlowTestName, long benchFlowTestNumber) {

    this.user = user;
    this.name = benchFlowTestName;
    this.number = benchFlowTestNumber;

    this.id =
        user.getUsername()
            + MODEL_ID_DELIMITER
            + benchFlowTestName
            + MODEL_ID_DELIMITER
            + benchFlowTestNumber;
    this.hashedID = this.id;

    this.state = START;
    this.runningState = DETERMINE_EXPLORATION_STRATEGY;
  }

  @PrePersist
  void prePersist() {
    lastModified = new Date();
  }

  public String getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getName() {
    return name;
  }

  public long getNumber() {
    return number;
  }

  public Date getStart() {
    return start;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public BenchFlowTestState getState() {
    return state;
  }

  public void setState(BenchFlowTestState state) {
    this.state = state;
  }

  public TestRunningState getRunningState() {
    return runningState;
  }

  public void setRunningState(TestRunningState runningState) {
    this.runningState = runningState;
  }

  public TestTerminatedState getTerminatedState() {
    return terminatedState;
  }

  public void setTerminatedState(TestTerminatedState terminatedState) {
    this.terminatedState = terminatedState;
  }

  public void addExperimentModel(BenchFlowExperimentModel experimentModel) {

    experiments.add(experimentModel);
  }

  public boolean containsExperimentModel(String experimentID) {

    return experiments.stream().filter(model -> model.getId().equals(experimentID)).count() != 0;
  }

  public List<Long> getExperimentNumbers() {

    return experiments
        .stream()
        .map(BenchFlowExperimentModel::getNumber)
        .collect(Collectors.toList());
  }

  public Set<BenchFlowExperimentModel> getExperimentModels() {

    return experiments;
  }

  public ExplorationModel getExplorationModel() {
    return explorationModel;
  }

  @JsonIgnore
  public long getNextExperimentNumber() {

    return experiments.size();
  }

  public enum BenchFlowTestState {
    START,
    READY,
    WAITING,
    RUNNING,
    TERMINATED
  }

  public enum TestRunningState {
    DETERMINE_EXPLORATION_STRATEGY,
    ADD_STORED_KNOWLEDGE,
    DETERMINE_EXECUTE_EXPERIMENTS,
    HANDLE_EXPERIMENT_RESULT,
    VALIDATE_TERMINATION_CRITERIA,
    DERIVE_PREDICTION_FUNCTION,
    VALIDATE_PREDICTION_FUNCTION,
    REMOVE_NON_REACHABLE_EXPERIMENTS
  }

  public enum TestTerminatedState {
    PARTIALLY_COMPLETE,
    COMPLETED_WITH_FAILURE,
    GOAL_REACHED
  }
}
