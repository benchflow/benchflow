package cloud.benchflow.testmanager.resources;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER_REGEX;

import cloud.benchflow.faban.client.responses.RunStatus;
import cloud.benchflow.testmanager.api.request.SubmitTrialStatusRequest;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTrialIDWebException;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17.
 */
public class BenchFlowTrialResourceTest {

  private BenchFlowTrialResource resource;
  private SubmitTrialStatusRequest request;

  // mocks
  private BenchFlowExperimentModelDAO experimentModelDAOMock =
      Mockito.mock(BenchFlowExperimentModelDAO.class);

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() throws Exception {

    resource = new BenchFlowTrialResource(experimentModelDAOMock);
    request = new SubmitTrialStatusRequest();
  }

  @Test
  public void submitTrialStatus() throws Exception {

    String experimentID = TestConstants.VALID_EXPERIMENT_ID;
    int trialNumber = 1;
    String trialID = experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + trialNumber;
    request.setStatus(RunStatus.StatusCode.COMPLETED);

    String[] trialIDArray = trialID.split(MODEL_ID_DELIMITER_REGEX);

    String username = trialIDArray[0];
    String testName = trialIDArray[1];
    int testNumber = Integer.parseInt(trialIDArray[2]);
    int experimentNumber = Integer.parseInt(trialIDArray[3]);

    resource.submitTrialStatus(username, testName, testNumber, experimentNumber, trialNumber,
        request);

    Mockito.verify(experimentModelDAOMock, Mockito.times(1)).addTrialStatus(experimentID,
        trialNumber, request.getStatus());
  }

  @Test
  public void submitInvalidTrialStatus() throws Exception {

    String experimentID = TestConstants.VALID_EXPERIMENT_ID;
    int trialNumber = 1;

    String trialID = experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + trialNumber;

    request.setStatus(RunStatus.StatusCode.COMPLETED);

    Mockito.doThrow(BenchFlowExperimentIDDoesNotExistException.class).when(experimentModelDAOMock)
        .addTrialStatus(experimentID, trialNumber, request.getStatus());
    exception.expect(InvalidTrialIDWebException.class);

    String[] trialIDArray = trialID.split(MODEL_ID_DELIMITER_REGEX);

    String username = trialIDArray[0];
    String testName = trialIDArray[1];
    int testNumber = Integer.parseInt(trialIDArray[2]);
    int experimentNumber = Integer.parseInt(trialIDArray[3]);

    resource.submitTrialStatus(username, testName, testNumber, experimentNumber, trialNumber,
        request);
  }
}
