package cloud.benchflow.experimentmanager.api.request;

import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 27.02.17.
 */
public class BenchFlowExperimentStateRequest {

    @NotNull
    @JsonProperty
    private BenchFlowExperimentModel.BenchFlowExperimentState state;

    @NotNull
    @JsonProperty
    private BenchFlowExperimentModel.BenchFlowExperimentStatus status;

    public BenchFlowExperimentStateRequest() {
    }

    public BenchFlowExperimentStateRequest(BenchFlowExperimentModel.BenchFlowExperimentState state, BenchFlowExperimentModel.BenchFlowExperimentStatus status) {
        this.state = state;
        this.status = status;
    }

    public BenchFlowExperimentModel.BenchFlowExperimentState getState() {
        return state;
    }

    public void setState(BenchFlowExperimentModel.BenchFlowExperimentState state) {
        this.state = state;
    }

    public BenchFlowExperimentModel.BenchFlowExperimentStatus getStatus() {
        return status;
    }

    public void setStatus(BenchFlowExperimentModel.BenchFlowExperimentStatus status) {
        this.status = status;
    }
}
