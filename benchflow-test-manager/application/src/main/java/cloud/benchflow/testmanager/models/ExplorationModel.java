package cloud.benchflow.testmanager.models;

import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;

import java.util.List;

import org.mongodb.morphia.annotations.Embedded;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-25
 */
@Embedded
public class ExplorationModel {

  private List<Integer> workloadUsersSpace = null;

  private ExperimentSelectionStrategy.Type experimentSelectionType;

  private boolean hasRegressionModel;

  public List<Integer> getWorkloadUsersSpace() {
    return workloadUsersSpace;
  }

  public void setWorkloadUsersSpace(List<Integer> workloadUsersSpace) {
    this.workloadUsersSpace = workloadUsersSpace;
  }

  public ExperimentSelectionStrategy.Type getExperimentSelectionType() {
    return experimentSelectionType;
  }

  public void setExperimentSelectionType(ExperimentSelectionStrategy.Type experimentSelectionType) {
    this.experimentSelectionType = experimentSelectionType;
  }

  public boolean hasRegressionModel() {
    return hasRegressionModel;
  }

  public void setHasRegressionModel(boolean hasRegressionModel) {
    this.hasRegressionModel = hasRegressionModel;
  }
}
