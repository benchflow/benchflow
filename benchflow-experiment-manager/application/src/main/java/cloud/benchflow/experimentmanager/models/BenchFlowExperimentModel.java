package cloud.benchflow.experimentmanager.models;

import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-03-23
 */
@Entity
@Indexes({@Index(options = @IndexOptions(), fields = {@Field(value = "hashedID", type = IndexType.HASHED)})})
public class BenchFlowExperimentModel {

    public static final String ID_FIELD_NAME = "id";
    public static final String HASHED_ID_FIELD_NAME = "hashedID";
    @Id
    private String id;
    // used for potential sharding in the future
    private String hashedID;
    private Date start = new Date();
    private Date lastModified = new Date();
    private BenchFlowExperimentState state = BenchFlowExperimentState.READY;
    private BenchFlowExperimentStatus status = BenchFlowExperimentStatus.READY;
    @Reference
    private List<TrialModel> trials = new ArrayList<>();

    BenchFlowExperimentModel() {
        // Empty constructor for MongoDB + Morphia
    }

    public BenchFlowExperimentModel(String experimentID) {

        this.id = experimentID;

        this.hashedID = this.id;

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

    public BenchFlowExperimentStatus getStatus() {
        return status;
    }

    public void setStatus(BenchFlowExperimentStatus status) {
        this.status = status;
    }

    public void addTrial(TrialModel trialModel) {

        trials.add(trialModel);

    }

    public enum BenchFlowExperimentState {READY, RUNNING, TERMINATED}

    public enum BenchFlowExperimentStatus {READY, NEW_TRIAL, HANDLE_RESULT, CHECK_CRITERIA, RE_EXECUTE_TRIAL, COMPLETED, FAILURE, ABORTED, ERROR}
}
