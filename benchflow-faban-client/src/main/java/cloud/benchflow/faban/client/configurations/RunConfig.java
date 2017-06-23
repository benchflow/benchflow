package cloud.benchflow.faban.client.configurations;

import cloud.benchflow.faban.client.responses.RunId;

/**
 * Configuration class for the run command.
 *
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 28/10/15.
 * @author vincenzoferme
 */
public class RunConfig implements Config {

  private RunId runId;

  public RunConfig(RunId runId) {
    this.runId = runId;
  }

  public RunId getRunId() {
    return runId;
  }

}
