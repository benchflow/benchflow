package cloud.benchflow.experimentmanager.resources;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.helpers.TestConstants;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.ws.rs.WebApplicationException;
import java.util.concurrent.ExecutorService;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER_REGEX;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-13
 */
public class BenchFlowExperimentResourceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();


    private MinioService minioMock = mock(MinioService.class);
    private BenchFlowExperimentModelDAO experimentModelDAO = mock(BenchFlowExperimentModelDAO.class);
    private FabanClient fabanMock = mock(FabanClient.class);
    private DriversMakerService driversMakerMock = mock(DriversMakerService.class);
    private ExecutorService taskExecutorService = mock(ExecutorService.class);
    private BenchFlowTestManagerService testManagerService = mock(BenchFlowTestManagerService.class);

    private BenchFlowExperimentResource experimentResource;

    @Before
    public void setUp() throws Exception {

        int submitRetries = 1;

        experimentResource = new BenchFlowExperimentResource(
                minioMock,
                experimentModelDAO,
                fabanMock,
                driversMakerMock,
                taskExecutorService,
                testManagerService,
                submitRetries
        );

    }

    @Test
    public void validRequest() throws Exception {

        String experimentID = TestConstants.BENCHFLOW_EXPERIMENT_ID;

        Mockito.doReturn(true).when(minioMock).isValidExperimentID(experimentID);

        String[] experimentIDArray = experimentID.split(MODEL_ID_DELIMITER_REGEX);
        String username = experimentIDArray[0];
        String testName = experimentIDArray[1];
        int testNumber = Integer.parseInt(experimentIDArray[2]);
        int experimentNumber = Integer.parseInt(experimentIDArray[3]);

        experimentResource.runBenchFlowExperiment(username, testName, testNumber, experimentNumber);

        Mockito.verify(taskExecutorService, times(1)).submit(Mockito.any(Runnable.class));
        Mockito.verify(minioMock, times(1)).isValidExperimentID(experimentID);

    }

    @Test
    public void invalidExperimentID() throws Exception {

        String experimentID = TestConstants.INVALID_BENCHFLOW_EXPERIMENT_ID;

        Mockito.doReturn(false).when(minioMock).isValidExperimentID(experimentID);

        String[] experimentIDArray = experimentID.split(MODEL_ID_DELIMITER_REGEX);
        String username = experimentIDArray[0];
        String testName = experimentIDArray[1];
        int testNumber = Integer.parseInt(experimentIDArray[2]);
        int experimentNumber = Integer.parseInt(experimentIDArray[3]);

        exception.expect(WebApplicationException.class);
        exception.expectMessage(BenchFlowConstants.INVALID_EXPERIMENT_ID_MESSAGE);

        experimentResource.runBenchFlowExperiment(username, testName, testNumber, experimentNumber);

        Mockito.verify(taskExecutorService, times(0)).submit(Mockito.any(Runnable.class));
        Mockito.verify(minioMock, times(1)).isValidExperimentID(experimentID);

    }

}