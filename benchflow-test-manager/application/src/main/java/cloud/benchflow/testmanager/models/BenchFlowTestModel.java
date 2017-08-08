package cloud.benchflow.testmanager.models;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.START;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.TestRunningState.DETERMINE_EXPLORATION_STRATEGY;

import cloud.benchflow.dsl.definition.types.time.Time;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.services.external.MinioService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
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
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 18.12.16.
 */
@Entity
@Indexes({@Index(options = @IndexOptions(),
    fields = {@Field(value = "hashedID", type = IndexType.HASHED)})})
@JsonIgnoreProperties(ignoreUnknown = true)
public class BenchFlowTestModel {

  /**
   * NOTE: This class is also annotated with Jackson annotation since we then easily can return it
   * when the user asks for the status of a given test. This annotation is not needed to store in
   * MongoDB.
   */
  public static final String ID_FIELD_NAME = "id";

  public static final String HASHED_ID_FIELD_NAME = "hashedID";
  @Id
  private String id;

  // Annotations for MongoDB + Morphia (http://mongodb.github.io/morphia/1.3/guides/annotations/#entity)

  //    userName.testName.testNumber.experimentNumber.trialNumber
  // used for potential sharing in the future
  @JsonIgnoreProperties(ignoreUnknown = true)
  private String hashedID;
  @Reference
  @JsonIgnore
  private User user;
  @JsonIgnore
  private String name;
  @JsonIgnore
  private long number;
  private Date start = new Date();
  private Date lastModified = new Date();
  private Time maxRunningTime;
  private BenchFlowTestState state;
  private TestRunningState runningState;
  private TestTerminatedState terminatedState;
  @Reference
  private TreeMap<Long, BenchFlowExperimentModel> experiments = new TreeMap<>();

  @JsonIgnore
  private ExplorationModel explorationModel = new ExplorationModel();

  private String testBundle;

  public BenchFlowTestModel() {
    // Empty constructor for MongoDB + Morphia
  }

  public BenchFlowTestModel(User user, String benchFlowTestName, long benchFlowTestNumber) {

    this.user = user;
    this.name = benchFlowTestName;
    this.number = benchFlowTestNumber;

    this.id = user.getUsername() + MODEL_ID_DELIMITER + benchFlowTestName + MODEL_ID_DELIMITER
        + benchFlowTestNumber;
    this.hashedID = this.id;

    this.state = START;
    this.runningState = DETERMINE_EXPLORATION_STRATEGY;

    this.testBundle = BenchFlowTestManagerApplication.getMinioServiceAddress() + "/minio/"
        + BenchFlowConstants.TESTS_BUCKET + "/" + MinioService.minioCompatibleID(id);
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

  public void setMaxRunningTime(Time maxRunningTime) {
    this.maxRunningTime = maxRunningTime;
  }

  public Time getMaxRunningTime() {
    return maxRunningTime;
  }

  public boolean hasMaxRunningTime() { return maxRunningTime!=null; }

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

    long experimentNumber =
        BenchFlowConstants.getExperimentNumberfromExperimentID(experimentModel.getId());

    experiments.put(experimentNumber, experimentModel);
  }

  public boolean containsExperimentModel(String experimentID) {

    long experimentNumber = BenchFlowConstants.getExperimentNumberfromExperimentID(experimentID);

    return experiments.containsKey(experimentNumber);
  }

  public TreeMap<Long, BenchFlowExperimentModel> getExperiments() {
    return experiments;
  }

  @JsonIgnore
  public Set<Long> getExperimentNumbers() {

    return experiments.keySet();
  }

  @JsonIgnore
  public Collection<BenchFlowExperimentModel> getExperimentModels() {

    return experiments.values();
  }

  public ExplorationModel getExplorationModel() {
    return explorationModel;
  }

  @JsonIgnore
  public long getNextExperimentNumber() {

    if (experiments.size() == 0) {
      return 1;
    }

    return experiments.lastKey() + 1;
  }

  public String getTestBundle() {
    return testBundle;
  }

  public enum BenchFlowTestState {
    START, READY, WAITING, RUNNING, TERMINATED
  }

  public enum TestRunningState {
    DETERMINE_EXPLORATION_STRATEGY, ADD_STORED_KNOWLEDGE, DETERMINE_EXECUTE_VALIDATION_SET, DETERMINE_EXECUTE_EXPERIMENTS, HANDLE_EXPERIMENT_RESULT, VALIDATE_TERMINATION_CRITERIA, DERIVE_PREDICTION_FUNCTION, VALIDATE_PREDICTION_FUNCTION, REMOVE_NON_REACHABLE_EXPERIMENTS
  }

  public enum TestTerminatedState {
    PARTIALLY_COMPLETE, COMPLETED_WITH_FAILURE, GOAL_REACHED
  }
}
