package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public class OneAtATimeSelectionStrategy extends SelectionStrategy {

  private static Logger logger =
      LoggerFactory.getLogger(OneAtATimeSelectionStrategy.class.getSimpleName());

  public OneAtATimeSelectionStrategy() {
    super(logger);
  }

  // only used for testing
  public OneAtATimeSelectionStrategy(MinioService minioService,
      ExplorationModelDAO explorationModelDAO) {

    super(logger, minioService, explorationModelDAO);
  }

  @Override
  protected int getNextExplorationPoint(List<Integer> executedExplorationPointIndices,
      int explorationSpaceSize) {

    // next experiment to be executed
    int nextExplorationPoint;
    // if list is empty we take the first
    if (executedExplorationPointIndices.size() == 0) {
      nextExplorationPoint = 0;
    } else {
      // get the max index and add 1 for the next
      nextExplorationPoint =
          executedExplorationPointIndices.stream().max(Integer::compareTo).orElse(0) + 1;
    }

    return nextExplorationPoint;
  }

}
