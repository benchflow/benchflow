package cloud.benchflow.testmanager.api.request;

import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 16.02.17.
 */
public class ChangeBenchFlowTestStateRequest {

  @NotNull
  @JsonProperty
  private BenchFlowTestModel.BenchFlowTestState state;

  public ChangeBenchFlowTestStateRequest() {}

  public ChangeBenchFlowTestStateRequest(BenchFlowTestModel.BenchFlowTestState state) {
    this.state = state;
  }

  public BenchFlowTestModel.BenchFlowTestState getState() {
    return state;
  }

  public void setState(BenchFlowTestModel.BenchFlowTestState state) {
    this.state = state;
  }
}
