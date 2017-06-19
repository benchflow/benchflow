package cloud.benchflow.testmanager.models;

import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.ExplorationSpace;
import cloud.benchflow.dsl.explorationspace.ExplorationSpaceGenerator.ExplorationSpaceDimensions;
import cloud.benchflow.testmanager.strategy.regression.RegressionStrategy;
import cloud.benchflow.testmanager.strategy.selection.SelectionStrategy;
import cloud.benchflow.testmanager.strategy.validation.ValidationStrategy;
import cloud.benchflow.testmanager.strategy.validation.ValidationStrategy.Type;
import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.annotations.Embedded;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-25
 */
@Embedded
public class ExplorationModel {

  private GoalType goalType;

  private ExplorationSpaceDimensions explorationSpaceDimensions;
  private ExplorationSpace explorationSpace;

  private List<Integer> executedExplorationPointIndices = new ArrayList<>();

  private SelectionStrategy.Type selectionStrategyType;
  private ValidationStrategy.Type validationStrategyType;
  private RegressionStrategy.Type regressionStrategyType;

  private boolean hasRegressionModel;

  public GoalType getGoalType() {
    return goalType;
  }

  public void setGoalType(GoalType goalType) {
    this.goalType = goalType;
  }

  public ExplorationSpaceDimensions getExplorationSpaceDimensions() {
    return explorationSpaceDimensions;
  }

  public void setExplorationSpaceDimensions(ExplorationSpaceDimensions explorationSpaceDimensions) {
    this.explorationSpaceDimensions = explorationSpaceDimensions;
  }

  public ExplorationSpace getExplorationSpace() {
    return explorationSpace;
  }

  public void setExplorationSpace(ExplorationSpace explorationSpace) {
    this.explorationSpace = explorationSpace;
  }

  public List<Integer> getExecutedExplorationPointIndices() {
    return executedExplorationPointIndices;
  }

  public void addExecutedExplorationPoint(int executedExplorationPointIndex) {
    this.executedExplorationPointIndices.add(executedExplorationPointIndex);
  }

  public SelectionStrategy.Type getSelectionStrategyType() {
    return selectionStrategyType;
  }

  public void setSelectionStrategyType(SelectionStrategy.Type selectionStrategyType) {
    this.selectionStrategyType = selectionStrategyType;
  }

  public Type getValidationStrategyType() {
    return validationStrategyType;
  }

  public void setValidationStrategyType(Type validationStrategyType) {
    this.validationStrategyType = validationStrategyType;
  }

  public RegressionStrategy.Type getRegressionStrategyType() {
    return regressionStrategyType;
  }

  public void setRegressionStrategyType(RegressionStrategy.Type regressionStrategyType) {
    this.regressionStrategyType = regressionStrategyType;
  }

  public boolean hasRegressionModel() {
    return hasRegressionModel;
  }

  public void setHasRegressionModel(boolean hasRegressionModel) {
    this.hasRegressionModel = hasRegressionModel;
  }

  public enum GoalType {
    LOAD, CONFIGURATION, EXPLORATION
  }

}
