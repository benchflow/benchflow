package cloud.benchflow.faban.client.configurations;

import cloud.benchflow.faban.client.responses.RunId;

/**
 * @author Simone D'Avico <simonedavico@gmail.com>
 *
 * Created on 28/10/15.
 */
public class StatusConfig implements Config {

  private RunId runId;

  public StatusConfig(RunId runId) {
    this.runId = runId;
  }

  public RunId getRunId() {
    return runId;
  }

}
