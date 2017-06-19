package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-06-17
 */
public class RandomBreakdownSelectionStrategyTest {

  private MinioService minioServiceMock = Mockito.mock(MinioService.class);
  private ExplorationModelDAO explorationModelDAO = Mockito.mock(ExplorationModelDAO.class);

  @Test
  public void getNextExplorationPoint() throws Exception {

    RandomBreakdownSelectionStrategy selectionStrategy =
        new RandomBreakdownSelectionStrategy(minioServiceMock, explorationModelDAO);

    int explorationSpaceSize = 50;

    List<List<Integer>> generatedExecutionOrders = new ArrayList<>();

    // generate three orders
    for (int i = 0; i < 3; i++) {
      generatedExecutionOrders.add(new ArrayList<>());
      for (int j = 0; j < explorationSpaceSize; j++) {
        List<Integer> generatedOrder = generatedExecutionOrders.get(i);
        generatedOrder
            .add(selectionStrategy.getNextExplorationPoint(generatedOrder, explorationSpaceSize));
      }
    }

    // check that the orders are different
    Assert.assertNotEquals(generatedExecutionOrders.get(0), generatedExecutionOrders.get(1));
    Assert.assertNotEquals(generatedExecutionOrders.get(0), generatedExecutionOrders.get(2));
    Assert.assertNotEquals(generatedExecutionOrders.get(1), generatedExecutionOrders.get(2));

  }

}
