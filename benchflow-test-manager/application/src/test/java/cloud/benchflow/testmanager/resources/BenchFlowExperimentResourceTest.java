package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.testmanager.helpers.TestConstants;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER_REGEX;
import static cloud.benchflow.testmanager.models.BenchFlowExperimentModel.*;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-17
 */
public class BenchFlowExperimentResourceTest {

    private BenchFlowExperimentResource resource;
    private BenchFlowExperimentStateRequest request;

    // mocks
    private BenchFlowExperimentModelDAO experimentModelDAOMock = Mockito.mock(BenchFlowExperimentModelDAO.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        resource = new BenchFlowExperimentResource(experimentModelDAOMock);
        request = new BenchFlowExperimentStateRequest();

    }

    @Test
    public void submitExperimentStatus() throws Exception {

        String experimentID = TestConstants.BENCHFLOW_EXPERIMENT_ID;

        request.setState(BenchFlowExperimentState.TERMINATED);
        request.setStatus(BenchFlowExperimentStatus.COMPLETED);

        String[] experimentIDArray = experimentID.split(MODEL_ID_DELIMITER_REGEX);
        String username = experimentIDArray[0];
        String testName = experimentIDArray[1];
        int testNumber = Integer.parseInt(experimentIDArray[2]);
        int experimentNumber = Integer.parseInt(experimentIDArray[3]);

        resource.setExperimentState(username, testName, testNumber, experimentNumber, request);

        Mockito.verify(experimentModelDAOMock, Mockito.times(1)).setExperimentState(experimentID, request.getState(), request.getStatus());
    }

    @Test
    public void submitInvalidExperimentStatus() throws Exception {
        // TODO - implement me
    }
}