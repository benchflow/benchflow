package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.testmanager.helpers.TestConstants;
import cloud.benchflow.testmanager.helpers.TestFiles;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.tasks.start.StartTask;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-27 */
public class CompleteSelectionStrategyTest {

  private MinioService minioMock = Mockito.mock(MinioService.class);
  private ExplorationModelDAO explorationModelDAOMock = Mockito.mock(ExplorationModelDAO.class);
  private BenchFlowTestModelDAO testModelDAOMock = Mockito.mock(BenchFlowTestModelDAO.class);

  private CompleteSelectionStrategy completeSelectionStrategy;

  @Before
  public void setUp() throws Exception {

    completeSelectionStrategy =
        new CompleteSelectionStrategy(minioMock, explorationModelDAOMock, testModelDAOMock);
  }

  @Test
  public void selectNextExperiment() throws Exception {

    String testID = TestConstants.VALID_TEST_ID;

    String expectedNumUsers = "5";

    Mockito.doReturn(TestFiles.getTestExplorationCompleteUsersInputStream())
        .when(minioMock)
        .getTestDefinition(testID);

    String testYaml =
        IOUtils.toString(
            TestFiles.getTestExplorationCompleteUsersInputStream(), StandardCharsets.UTF_8);

    BenchFlowTest test = BenchFlowDSL.testFromYaml(testYaml);

    List<Integer> selectionStrategy = StartTask.generateExplorationSpace(test);

    Mockito.doReturn(selectionStrategy).when(explorationModelDAOMock).getWorkloadUserSpace(testID);

    Set<Long> experimentNumbers = new HashSet<>();
    // ensure that experiment is available in DB
    experimentNumbers.add(0L);

    Mockito.doReturn(experimentNumbers).when(testModelDAOMock).getExperimentNumbers(testID);

    String experimentYaml = completeSelectionStrategy.selectNextExperiment(testID);

    Assert.assertNotNull(experimentYaml);
    Assert.assertTrue(experimentYaml.contains("users: " + expectedNumUsers));

    // run the next experiment
    // make sure input stream has not been read already
    Mockito.doReturn(TestFiles.getTestExplorationCompleteUsersInputStream())
        .when(minioMock)
        .getTestDefinition(testID);

    experimentNumbers.add(1L);

    experimentYaml = completeSelectionStrategy.selectNextExperiment(testID);

    expectedNumUsers = "10";

    Assert.assertNotNull(experimentYaml);
    Assert.assertTrue(experimentYaml.contains("users: " + expectedNumUsers));
  }
}
