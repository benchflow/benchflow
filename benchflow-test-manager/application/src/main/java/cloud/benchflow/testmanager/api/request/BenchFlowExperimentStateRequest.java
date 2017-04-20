package cloud.benchflow.testmanager.api.request;

import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 16.02.17.
 */
public class BenchFlowExperimentStateRequest {

    @NotNull
    @JsonProperty
    private BenchFlowExperimentState state;

    @NotNull
    @JsonProperty
    private BenchFlowExperimentStatus status;

    public BenchFlowExperimentStateRequest() {
    }

    public BenchFlowExperimentStateRequest(BenchFlowExperimentState state, BenchFlowExperimentStatus status) {
        this.state = state;
        this.status = status;
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
}
