package cloud.benchflow.faban.client.responses;

import cloud.benchflow.faban.client.exceptions.IllegalRunIdException;

/**
 *
 * @author Simone D'Avico <simonedavico@gmail.com>
 */
public class RunId implements Response {

    private String name;
    private String queueId;

    public RunId(String name, String queueId) {
        this.name = name;
        this.queueId = queueId;
    }

    public RunId(String runId) {
        String[] parts = runId.split("\\.");
        if(parts.length != 2)
            throw new IllegalRunIdException("Received unexpected runId " + runId);
        this.name = parts[0];
        this.queueId = parts[1];
    }

    @Override
    public String toString() {
        return this.name + "." + this.queueId;
    }

}
