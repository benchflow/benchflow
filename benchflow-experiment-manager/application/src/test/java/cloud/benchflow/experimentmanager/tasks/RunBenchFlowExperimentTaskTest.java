package cloud.benchflow.experimentmanager.tasks;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.helpers.MinioTestData;
import cloud.benchflow.experimentmanager.helpers.TestConstants;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.InputStream;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-13
 */
public class RunBenchFlowExperimentTaskTest {


    private RunBenchFlowExperimentTask experimentTask;
    private String experimentID;
    private RunBenchFlowExperimentTask.DriversMakerCompatibleID driversMakerCompatibleID;
    private int submitRetries = 1;

    private BenchFlowExperimentModelDAO experimentModelDAOMock = Mockito.mock(BenchFlowExperimentModelDAO.class);
    private MinioService minioServiceMock = Mockito.mock(MinioService.class);
    private FabanClient fabanClientMock = Mockito.mock(FabanClient.class);
    private DriversMakerService driversMakerServiceMock = Mockito.mock(DriversMakerService.class);
    private BenchFlowTestManagerService testManagerServiceMock = Mockito.mock(BenchFlowTestManagerService.class);

    private InputStream experimentDefinitionInputStream;
    private InputStream deploymentDescriptorInputStream;
    private InputStream bpmnModel11InputStream;
    private InputStream generatedBenchmarkInputStream;
    private InputStream fabanConfigurationInputStream;


    @Before
    public void setUp() throws Exception {

        experimentID = TestConstants.BENCHFLOW_EXPERIMENT_ID;
        driversMakerCompatibleID = new RunBenchFlowExperimentTask.DriversMakerCompatibleID().invoke(experimentID);

        experimentDefinitionInputStream = MinioTestData.getExperimentDefinition();
        deploymentDescriptorInputStream = MinioTestData.getDeploymentDescriptor();
        bpmnModel11InputStream = MinioTestData.get11ParallelStructuredModel();
        generatedBenchmarkInputStream = MinioTestData.getGeneratedBenchmark();
        fabanConfigurationInputStream = MinioTestData.getFabanConfiguration();


        experimentTask = new RunBenchFlowExperimentTask(
                experimentID,
                experimentModelDAOMock,
                minioServiceMock,
                fabanClientMock,
                driversMakerServiceMock,
                testManagerServiceMock,
                submitRetries
        );

    }

    @Test
    public void run() throws Exception {

        int nTrials = 1;

        String fabanID = "test_faban_id";
        RunId runId = new RunId(fabanID, "1");
        RunStatus status = new RunStatus("COMPLETED", runId);
        String trialID = experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + 1;

        Mockito.doReturn(experimentDefinitionInputStream)
                .when(minioServiceMock)
                .getExperimentDefinition(experimentID);

        Mockito.doReturn(deploymentDescriptorInputStream)
                .when(minioServiceMock)
                .getExperimentDeploymentDescriptor(experimentID);

        Mockito.doReturn(bpmnModel11InputStream)
                .when(minioServiceMock)
                .getExperimentBPMNModel(experimentID, MinioTestData.BPM_MODEL_11_PARALLEL_NAME);

        Mockito.doReturn(generatedBenchmarkInputStream)
                .when(minioServiceMock)
                .getDriversMakerGeneratedBenchmark(
                        driversMakerCompatibleID.getDriversMakerExperimentID(),
                        driversMakerCompatibleID.getExperimentNumber()
                );

        Mockito.doReturn(trialID)
                .when(experimentModelDAOMock)
                .addTrial(experimentID, 1);

        Mockito.doReturn(fabanConfigurationInputStream)
                .when(minioServiceMock)
                .getDriversMakerGeneratedFabanConfiguration(
                        driversMakerCompatibleID.getDriversMakerExperimentID(),
                        driversMakerCompatibleID.getExperimentNumber(),
                        1
                );

        Mockito.doReturn(runId)
                .when(fabanClientMock)
                .submit(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class));

        Mockito.doReturn(status)
                .when(fabanClientMock)
                .status(runId);


        experimentTask.run();

        Mockito.verify(experimentModelDAOMock, Mockito.times(1)).addExperiment(experimentID);
        Mockito.verify(driversMakerServiceMock, Mockito.times(1)).generateBenchmark(Mockito.anyString(), Mockito.anyLong(), Mockito.eq(nTrials));
        Mockito.verify(fabanClientMock, Mockito.times(1)).deploy(Mockito.any(File.class));

        Mockito.verify(experimentModelDAOMock, Mockito.times(nTrials)).addTrial(Mockito.eq(experimentID), Mockito.anyLong());

        Mockito.verify(experimentModelDAOMock, Mockito.times(1)).setFabanTrialID(experimentID, 1, runId.toString());
        Mockito.verify(experimentModelDAOMock, Mockito.times(1)).setTrialModelAsStarted(experimentID, 1);
        Mockito.verify(experimentModelDAOMock, Mockito.times(1)).setTrialStatus(experimentID, 1, status.getStatus());

        Mockito.verify(testManagerServiceMock, Mockito.times(1)).submitTrialStatus(trialID, status.getStatus());



    }
}