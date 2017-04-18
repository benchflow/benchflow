package cloud.benchflow.testmanager.api.request;

import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 16.02.17.
 */
public class BenchFlowExperimentStateRequest {

    @NotNull
    @JsonProperty
    private BenchFlowExperimentModel.BenchFlowExperimentState state;

    public BenchFlowExperimentStateRequest() {
    }

    public BenchFlowExperimentStateRequest(BenchFlowExperimentModel.BenchFlowExperimentState state) {
        this.state = state;
    }

    public BenchFlowExperimentModel.BenchFlowExperimentState getState() {
        return state;
    }

    public void setState(BenchFlowExperimentModel.BenchFlowExperimentState state) {
        this.state = state;
    }
}
