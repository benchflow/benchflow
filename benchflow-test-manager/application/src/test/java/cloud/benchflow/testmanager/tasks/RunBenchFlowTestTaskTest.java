package cloud.benchflow.testmanager.tasks;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.testmanager.archive.BenchFlowTestArchiveExtractor;
import cloud.benchflow.testmanager.archive.TestArchives;
import cloud.benchflow.testmanager.exceptions.InvalidTestArchiveException;
import cloud.benchflow.testmanager.helpers.TestConstants;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 13.02.17.
 */
public class RunBenchFlowTestTaskTest {


    private RunBenchFlowTestTask task;
    private String testID;

    // Mocks
    private MinioService minioServiceMock = Mockito.mock(MinioService.class);
    private BenchFlowExperimentManagerService peManagerServiceMock = Mockito.mock(BenchFlowExperimentManagerService.class);
    private BenchFlowExperimentModelDAO experimentModelDAOMock = Mockito.mock(BenchFlowExperimentModelDAO.class);

    @Before
    public void setUp() throws Exception {

        testID = TestConstants.TEST_USER_NAME + MODEL_ID_DELIMITER + TestConstants.VALID_BENCHFLOW_TEST_NAME + MODEL_ID_DELIMITER + 1;

        ZipInputStream archiveZipInputStream = TestArchives.getValidTestArchiveZip();

        // validate archive
        // Get the contents of archive and check if valid Test ID
        String testDefinitionString = BenchFlowTestArchiveExtractor.extractBenchFlowTestDefinitionString(
                archiveZipInputStream);

        InputStream deploymentDescriptorInputStream = BenchFlowTestArchiveExtractor.extractDeploymentDescriptorInputStream(
                archiveZipInputStream);
        Map<String, InputStream> bpmnModelsInputStream = BenchFlowTestArchiveExtractor.extractBPMNModelInputStreams(
                archiveZipInputStream);

        task = new RunBenchFlowTestTask(
                testID,
                minioServiceMock,
                peManagerServiceMock,
                experimentModelDAOMock,
                testDefinitionString,
                deploymentDescriptorInputStream,
                bpmnModelsInputStream
        );

    }

    @Test
    public void run() throws Exception {

        String experimentID = testID + MODEL_ID_DELIMITER + "1";

        Mockito.doReturn(experimentID).when(experimentModelDAOMock).addExperiment(testID);

        task.run();

        // check: save benchFlowTestArchive to Minio
        verify(minioServiceMock, times(1))
                .saveTestDefinition(Mockito.eq(testID), Mockito.any(InputStream.class));

        verify(minioServiceMock, times(1))
                .saveTestDeploymentDescriptor(Mockito.eq(testID), Mockito.any(InputStream.class));

        verify(minioServiceMock, times(TestArchives.BPMN_MODELS_COUNT))
                .saveTestBPMNModel(Mockito.eq(testID), Mockito.anyString(), Mockito.any(InputStream.class));

        // check PE was saved to DAO
        verify(experimentModelDAOMock, times(1)).addExperiment(testID);

        // check: generate the experiment definition save to minio
        verify(minioServiceMock, times(1))
                .saveExperimentDefinition(Mockito.eq(experimentID),
                                                  Mockito.any(InputStream.class));

        // run PE on PEManager
        verify(peManagerServiceMock, times(1)).runBenchFlowExperiment(experimentID);

    }

}