package cloud.benchflow.testmanager.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexType;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.READY;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 18.12.16.
 */
@Entity
@Indexes({@Index(options = @IndexOptions(), fields = {@Field(value = "hashedID", type = IndexType.HASHED)})})
public class BenchFlowTestModel {

    /**
     * NOTE: This class is also annotated with Jackson annotation since we then easily can return it
     * when the user asks for the status of a given test. This annotation is not needed to store in MongoDB.
     */

    public static final String ID_FIELD_NAME = "id";
    public static final String HASHED_ID_FIELD_NAME = "hashedID";
    @Id
    private String id;

    // Annotations for MongoDB + Morphia (http://mongodb.github.io/morphia/1.3/guides/annotations/#entity)

    //    userName.testName.testNumber.experimentNumber.trialNumber
    // used for potential sharing in the future
    @JsonIgnore
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
    private BenchFlowTestState state;
    @Reference
    private Set<BenchFlowExperimentModel> experiments = new HashSet<>();

    public BenchFlowTestModel() {
        // Empty constructor for MongoDB + Morphia
    }

    public BenchFlowTestModel(User user, String benchFlowTestName, long benchFlowTestNumber) {

        this.user = user;
        this.name = benchFlowTestName;
        this.number = benchFlowTestNumber;

        this.id = user.getUsername() + MODEL_ID_DELIMITER + benchFlowTestName + MODEL_ID_DELIMITER + benchFlowTestNumber;
        this.hashedID = this.id;

        this.state = READY;

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

    public void addExperimentModel(BenchFlowExperimentModel experimentModel) {

        experiments.add(experimentModel);

    }

    public boolean containsExperimentModel(String experimentID) {

        return experiments.stream().filter(model -> model.getId().equals(experimentID)).count() != 0;

    }

    public Set<BenchFlowExperimentModel> getExperimentModels() {

        return experiments;

    }

    @JsonIgnore
    public long getNextExperimentNumber() {

        return experiments.size() + 1;

    }

    public enum BenchFlowTestState {READY, RUNNING, COMPLETED}

}
