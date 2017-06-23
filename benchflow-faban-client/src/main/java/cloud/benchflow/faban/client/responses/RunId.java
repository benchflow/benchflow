package cloud.benchflow.faban.client.responses;

import cloud.benchflow.faban.client.exceptions.IllegalRunIdException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 */
public class RunId implements Response {

  private String name;
  private String queueId;

  /**
   * Contruct a run id response.
   *
   * @param name the name of the benchmark
   * @param queueId the id of the benchmark in the queue
   */
  public RunId(String name, String queueId) {
    this.name = name;
    this.queueId = queueId;
  }

  /**
   * Contruct a run id response.
   *
   * @param runId the run id of the benchmark
   */
  public RunId(String runId) throws IllegalRunIdException {
    String[] parts = runId.split("\\.");
    if (parts.length != 2) {
      throw new IllegalRunIdException("Received unexpected runId " + runId);
    }
    this.name = parts[0];
    this.queueId = parts[1];
  }

  @Override
  public String toString() {
    return this.name + "." + this.queueId;
  }

}
