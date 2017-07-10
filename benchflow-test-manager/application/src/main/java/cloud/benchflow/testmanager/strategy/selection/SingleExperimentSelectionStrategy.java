package cloud.benchflow.testmanager.strategy.selection;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-30
 */
public class SingleExperimentSelectionStrategy extends SelectionStrategy {

  private static Logger logger =
      LoggerFactory.getLogger(SingleExperimentSelectionStrategy.class.getSimpleName());

  public SingleExperimentSelectionStrategy() {
    super(logger);
  }

  @Override
  protected int getNextExplorationPoint(List<Integer> executedExplorationPointIndices,
      int explorationSpaceSize) {

    return 0;
  }
}
