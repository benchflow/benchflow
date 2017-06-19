package cloud.benchflow.testmanager.api.request;

import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.TerminatedState;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 16.02.17.
 */
public class BenchFlowExperimentStateRequest {

  @NotNull
  @JsonProperty
  private BenchFlowExperimentState state;

  @JsonProperty
  private RunningState runningState;

  @JsonProperty
  private TerminatedState terminatedState;

  public BenchFlowExperimentStateRequest() {}

  public BenchFlowExperimentStateRequest(BenchFlowExperimentState state,
      RunningState runningState) {
    this.state = state;
    this.runningState = runningState;
  }

  public BenchFlowExperimentStateRequest(BenchFlowExperimentState state,
      TerminatedState terminatedState) {
    this.state = state;
    this.terminatedState = terminatedState;
  }

  public BenchFlowExperimentState getState() {
    return state;
  }

  public void setState(BenchFlowExperimentState state) {
    this.state = state;
  }

  public RunningState getRunningState() {
    return runningState;
  }

  public void setRunningState(RunningState runningState) {
    this.runningState = runningState;
  }

  public TerminatedState getTerminatedState() {
    return terminatedState;
  }

  public void setTerminatedState(TerminatedState terminatedState) {
    this.terminatedState = terminatedState;
  }
}
