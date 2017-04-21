package cloud.benchflow.driversmaker.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 25/02/16.
 */
public class Experiment {

    private String userId;

    @JsonProperty("experimentName")
    private String experimentName;

    @JsonProperty("experimentNumber")
    private long experimentNumber;

    @JsonProperty("trials")
    private int totalTrials;

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public long getExperimentNumber() {
        return experimentNumber;
    }

    public void setExperimentNumber(long experimentNumber) {
        this.experimentNumber = experimentNumber;
    }

    public int getTotalTrials() {
        return totalTrials;
    }

    public void setTotalTrials(int totalTrials) {
        this.totalTrials = totalTrials;
    }

    public String getExperimentId() { return getBenchmarkId() + "." + experimentNumber; }

    public String getBenchmarkId() { return userId + "." + experimentName; }

    public Trial getTrial(int trialNumber) {
        if(trialNumber > totalTrials)
            throw new IllegalArgumentException("Supplied trial number exceeds total trials.");
        return new Trial(getBenchmarkId(), experimentNumber, trialNumber, totalTrials);
    }

    public Iterator<Trial> getAllTrials() {
        return new TrialsIterator();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private final class TrialsIterator implements Iterator<Trial> {

        private int cursor;

        public TrialsIterator() {
            this.cursor = 1;
        }

        @Override
        public boolean hasNext() {
            return cursor <= Experiment.this.totalTrials;
        }

        @Override
        public Trial next() {
            if(this.hasNext())
                return Experiment.this.getTrial(cursor++);
            throw new NoSuchElementException();
        }
    }
}
