package cloud.benchflow.faban.client.configurations;

import cloud.benchflow.faban.client.responses.RunId;

/**
 * Configuration class for the show logs command.
 *
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 11/11/15.
 */
//TODO: implement this
public class ShowLogsConfig implements Config {

  private RunId runId;

  public ShowLogsConfig(RunId runId) {
    this.runId = runId;
  }

  public RunId getRunId() {
    return runId;
  }

}
