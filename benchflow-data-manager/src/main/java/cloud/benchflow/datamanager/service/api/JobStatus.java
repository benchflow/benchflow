package cloud.benchflow.datamanager.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobStatus {
  private final int step;
  private final boolean finished;

  public JobStatus(int step, boolean finished) {
    this.step = step;
    this.finished = finished;
  }

  @JsonProperty
  public int getStep() {
    return step;
  }

  @JsonProperty
  public boolean isFinished() {
    return finished;
  }
}
