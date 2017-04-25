package cloud.benchflow.testmanager.models;

import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;

import java.util.List;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-25
 */
public class ExplorationModel {

    private List<Integer> workloadUsersSpace = null;

    private ExperimentSelectionStrategy experimentSelectionStrategy;

    public List<Integer> getWorkloadUsersSpace() {
        return workloadUsersSpace;
    }

    public void setWorkloadUsersSpace(List<Integer> workloadUsersSpace) {
        this.workloadUsersSpace = workloadUsersSpace;
    }

    public ExperimentSelectionStrategy getExperimentSelectionStrategy() {
        return experimentSelectionStrategy;
    }

    public void setExperimentSelectionStrategy(ExperimentSelectionStrategy experimentSelectionStrategy) {
        this.experimentSelectionStrategy = experimentSelectionStrategy;
    }

}
