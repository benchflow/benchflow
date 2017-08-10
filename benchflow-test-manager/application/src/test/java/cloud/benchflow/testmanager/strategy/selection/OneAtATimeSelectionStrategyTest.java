package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.helpers.constants.TestFiles;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.SelectionStrategy.SelectedExperimentBundle;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-27
 */
public class OneAtATimeSelectionStrategyTest {

  private MinioService minioMock = Mockito.mock(MinioService.class);
  private ExplorationModelDAO explorationModelDAOMock = Mockito.mock(ExplorationModelDAO.class);

  private OneAtATimeSelectionStrategy oneAtATimeSelectionStrategy;

  @Before
  public void setUp() throws Exception {

    oneAtATimeSelectionStrategy =
        new OneAtATimeSelectionStrategy(minioMock, explorationModelDAOMock);
  }

  @Test
  public void selectNextExperimentFirstIndex() throws Exception {

    String testID = TestConstants.LOAD_TEST_ID;

    // return test definition
    Mockito.doReturn(TestFiles.getTestExplorationOneAtATimeUsersMemoryEnvironmentInputStream())
        .when(minioMock).getTestDefinition(testID);
    // return deployment descriptor
    Mockito.doReturn(TestFiles.getDeploymentDescriptor()).when(minioMock)
        .getTestDeploymentDescriptor(testID);

    // TODO - change when converting to JavaCompatExplorationSpace works
    //    JavaCompatExplorationSpace explorationSpace = ExplorationSpaceAPI.explorationSpaceFromTestYaml(
    //        TestFiles.getTestExplorationOneAtATimeUsersMemoryEnvironmentString());
    //
    //    Mockito.doReturn(explorationSpace).when(explorationModelDAOMock).getExplorationSpace(testID);

    // return empty exploration point indices list
    Mockito.doReturn(new ArrayList<>()).when(explorationModelDAOMock)
        .getExecutedExplorationPointIndices(testID);

    SelectedExperimentBundle experimentBundle =
        oneAtATimeSelectionStrategy.selectNextExperiment(testID);

    Assert.assertEquals(0, experimentBundle.getExplorationSpaceIndex());

    // assert memory limit added
    Assert.assertTrue(
        experimentBundle.getDeploymentDescriptorYamlString().contains("mem_limit: 500m"));

    // assert environment changed
    Assert.assertTrue(
        experimentBundle.getDeploymentDescriptorYamlString().contains("SIZE_OF_THREADPOOL=1"));
    Assert.assertTrue(experimentBundle.getDeploymentDescriptorYamlString().contains("AN_ENUM=A"));

    // assert number of users is correct
    Assert.assertTrue(experimentBundle.getExperimentYamlString().contains("users: 5"));

  }
}
