package cloud.benchflow.experimentmanager.api.request;

import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 27.02.17.
 */
public class BenchFlowExperimentStatusRequest {

    @NotNull
    @JsonProperty
    private BenchFlowExperimentModel.BenchFlowExperimentStatus status;

    public BenchFlowExperimentStatusRequest() {
    }

    public BenchFlowExperimentStatusRequest(BenchFlowExperimentModel.BenchFlowExperimentStatus status) {
        this.status = status;
    }

    public BenchFlowExperimentModel.BenchFlowExperimentStatus getStatus() {
        return status;
    }

    public void setStatus(BenchFlowExperimentModel.BenchFlowExperimentStatus status) {
        this.status = status;
    }
}
