package cloud.benchflow.testmanager.strategy.selection;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20 */
public interface ExperimentSelectionStrategy {

  enum Type {
    COMPLETE_SELECTION
  }

  String selectNextExperiment(String testID);
}
