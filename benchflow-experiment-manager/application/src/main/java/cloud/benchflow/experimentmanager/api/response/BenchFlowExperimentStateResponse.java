package cloud.benchflow.experimentmanager.api.response;

import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 09.03.17. */
public class BenchFlowExperimentStateResponse {

  @NotEmpty
  @JsonProperty
  private BenchFlowExperimentModel.BenchFlowExperimentState state;

  public BenchFlowExperimentStateResponse(BenchFlowExperimentModel.BenchFlowExperimentState state) {
    this.state = state;
  }
}
