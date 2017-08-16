package cloud.benchflow.testmanager.models;

import cloud.benchflow.dsl.definition.configuration.goal.goaltype.GoalType;
import cloud.benchflow.dsl.definition.configuration.strategy.regression.RegressionStrategyType;
import cloud.benchflow.dsl.definition.configuration.strategy.selection.SelectionStrategyType;
import cloud.benchflow.dsl.definition.configuration.strategy.validation.ValidationStrategyType;
import cloud.benchflow.testmanager.models.explorationspace.MongoCompatibleExplorationSpace;
import cloud.benchflow.testmanager.models.explorationspace.MongoCompatibleExplorationSpaceDimensions;
import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.annotations.Embedded;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-25
 */
@Embedded
public class ExplorationModel {

  private GoalType goalType;

  private MongoCompatibleExplorationSpaceDimensions explorationSpaceDimensions;
  private MongoCompatibleExplorationSpace explorationSpace;

  private List<Integer> executedExplorationPointIndices = new ArrayList<>();

  private SelectionStrategyType selectionStrategyType;
  private ValidationStrategyType validationStrategyType;
  private RegressionStrategyType regressionStrategyType;

  private boolean hasRegressionModel;
  private boolean singleExperiment;

  public GoalType getGoalType() {
    return goalType;
  }

  public void setGoalType(GoalType goalType) {
    this.goalType = goalType;
  }

  public MongoCompatibleExplorationSpaceDimensions getExplorationSpaceDimensions() {
    return explorationSpaceDimensions;
  }

  public void setExplorationSpaceDimensions(
      MongoCompatibleExplorationSpaceDimensions explorationSpaceDimensions) {
    this.explorationSpaceDimensions = explorationSpaceDimensions;
  }

  public MongoCompatibleExplorationSpace getExplorationSpace() {
    return explorationSpace;
  }

  public void setExplorationSpace(MongoCompatibleExplorationSpace explorationSpace) {
    this.explorationSpace = explorationSpace;
  }

  public List<Integer> getExecutedExplorationPointIndices() {
    return executedExplorationPointIndices;
  }

  public void addExecutedExplorationPoint(int executedExplorationPointIndex) {
    this.executedExplorationPointIndices.add(executedExplorationPointIndex);
  }

  public SelectionStrategyType getSelectionStrategyType() {
    return selectionStrategyType;
  }

  public void setSelectionStrategyType(SelectionStrategyType selectionStrategyType) {
    this.selectionStrategyType = selectionStrategyType;
  }

  public ValidationStrategyType getValidationStrategyType() {
    return validationStrategyType;
  }

  public void setValidationStrategyType(ValidationStrategyType validationStrategyType) {
    this.validationStrategyType = validationStrategyType;
  }

  public RegressionStrategyType getRegressionStrategyType() {
    return regressionStrategyType;
  }

  public void setRegressionStrategyType(RegressionStrategyType regressionStrategyType) {
    this.regressionStrategyType = regressionStrategyType;
  }

  public boolean hasRegressionModel() {
    return hasRegressionModel;
  }

  public void setHasRegressionModel(boolean hasRegressionModel) {
    this.hasRegressionModel = hasRegressionModel;
  }

  public boolean isSingleExperiment() {
    return singleExperiment;
  }

  public void setSingleExperiment(boolean singleExperiment) {
    this.singleExperiment = singleExperiment;
  }

}
