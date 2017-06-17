package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-05
 */
public class RandomBreakdownSelectionStrategy extends SelectionStrategy {

  private static Logger logger =
      LoggerFactory.getLogger(RandomBreakdownSelectionStrategy.class.getSimpleName());

  public RandomBreakdownSelectionStrategy() {
    super(logger);
  }

  public RandomBreakdownSelectionStrategy(MinioService minioService,
      ExplorationModelDAO explorationModelDAO) {
    super(logger, minioService, explorationModelDAO);
  }


  @Override
  protected int getNextExplorationPoint(List<Integer> executedExplorationPointIndices,
      int explorationSpaceSize) {

    Random random = new Random();

    int nextIndex = random.nextInt(explorationSpaceSize);

    while (executedExplorationPointIndices.contains(nextIndex)) {
      nextIndex = random.nextInt(explorationSpaceSize);
    }

    return nextIndex;

  }

}
