package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.testmanager.archive.TestArchives;
import cloud.benchflow.testmanager.helpers.TestConstants;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-27
 */
public class CompleteSelectionStrategyTest {

    private MinioService minioMock = Mockito.mock(MinioService.class);
    private ExplorationModelDAO explorationModelDAOMock = Mockito.mock(ExplorationModelDAO.class);
    private BenchFlowTestModelDAO testModelDAOMock = Mockito.mock(BenchFlowTestModelDAO.class);

    private CompleteSelectionStrategy completeSelectionStrategy;

    @Before
    public void setUp() throws Exception {

        completeSelectionStrategy = new CompleteSelectionStrategy(minioMock, explorationModelDAOMock, testModelDAOMock);

    }

    @Test
    public void selectNextExperiment() throws Exception {

        String testID = TestConstants.VALID_TEST_ID;

        InputStream testDefinitionInputStream = TestArchives.getValidTestDefinitionInputStream();

        Mockito.doReturn(testDefinitionInputStream)
                .when(minioMock)
                .getTestDefinition(testID);


    }
}