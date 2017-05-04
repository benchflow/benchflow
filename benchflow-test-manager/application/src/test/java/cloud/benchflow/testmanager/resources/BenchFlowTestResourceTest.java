package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.api.request.ChangeBenchFlowTestStateRequest;
import cloud.benchflow.testmanager.api.response.ChangeBenchFlowTestStateResponse;
import cloud.benchflow.testmanager.api.response.RunBenchFlowTestResponse;
import cloud.benchflow.testmanager.archive.TestArchives;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.web.InvalidBenchFlowTestIDWebException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTestArchiveWebException;
import cloud.benchflow.testmanager.helpers.TestConstants;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import cloud.benchflow.testmanager.tasks.BenchFlowTestTaskController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;
import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER_REGEX;
import static cloud.benchflow.testmanager.helpers.TestConstants.*;
import static cloud.benchflow.testmanager.helpers.TestConstants.TEST_USER_NAME;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.TERMINATED;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.RUNNING;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17. */
public class BenchFlowTestResourceTest {

  // mocks
  private BenchFlowTestModelDAO testModelDAOMock = mock(BenchFlowTestModelDAO.class);
  private UserDAO userDAOMock = mock(UserDAO.class);
  private BenchFlowTestTaskController testTaskController = mock(BenchFlowTestTaskController.class);

  private BenchFlowTestResource resource;
  private ChangeBenchFlowTestStateRequest request;

  @Rule public ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() throws Exception {

    resource = new BenchFlowTestResource(testModelDAOMock, userDAOMock, testTaskController);
    request = new ChangeBenchFlowTestStateRequest();
  }

  @Test
  public void runBenchFlowTestEmptyRequest() throws Exception {

    exception.expect(WebApplicationException.class);
    exception.expectMessage(String.valueOf(Response.Status.BAD_REQUEST.getStatusCode()));

    resource.runBenchFlowTest(TEST_USER_NAME, null);
  }

  @Test
  public void runBenchFlowTestValid() throws Exception {

    InputStream expArchive = TestArchives.getValidTestArchive();

    String expectedTestID =
        TEST_USER_NAME
            + MODEL_ID_DELIMITER
            + TestConstants.VALID_BENCHFLOW_TEST_NAME
            + MODEL_ID_DELIMITER
            + 1;

    Mockito.doReturn(expectedTestID)
        .when(testModelDAOMock)
        .addTestModel(TestConstants.VALID_BENCHFLOW_TEST_NAME, BenchFlowConstants.BENCHFLOW_USER);

    RunBenchFlowTestResponse response = resource.runBenchFlowTest(TEST_USER_NAME, expArchive);

    Assert.assertTrue(response.getTestID().contains(VALID_BENCHFLOW_TEST_NAME));

    verify(testTaskController, times(1))
        .startTest(
            Mockito.matches(expectedTestID),
            Mockito.any(String.class),
            Mockito.any(InputStream.class),
            Mockito.anyMap());
  }

  @Test
  public void runInvalidBenchFlowTest() throws Exception {

    InputStream expArchive = TestArchives.getNoDefinitionTestArchive();

    exception.expect(InvalidTestArchiveWebException.class);

    resource.runBenchFlowTest(TEST_USER_NAME, expArchive);
  }

  @Test
  public void changeBenchFlowTestState() throws Exception {

    Mockito.doReturn(RUNNING).when(testModelDAOMock).setTestState(VALID_BENCHFLOW_TEST_ID, RUNNING);
    Mockito.doReturn(TERMINATED)
        .when(testModelDAOMock)
        .setTestState(VALID_BENCHFLOW_TEST_ID, TERMINATED);

    request.setState(RUNNING);

    String[] testIDArray = VALID_BENCHFLOW_TEST_ID.split(MODEL_ID_DELIMITER_REGEX);

    String username = testIDArray[0];
    String testName = testIDArray[1];
    int testNumber = Integer.parseInt(testIDArray[2]);

    ChangeBenchFlowTestStateResponse response =
        resource.changeBenchFlowTestState(username, testName, testNumber, request);

    Assert.assertNotNull(response);
    Assert.assertEquals(RUNNING, response.getState());

    request.setState(TERMINATED);

    response = resource.changeBenchFlowTestState(username, testName, testNumber, request);

    Assert.assertNotNull(response);
    Assert.assertEquals(TERMINATED, response.getState());
  }

  @Test
  public void changeBenchFlowTestStateInvalid() throws Exception {

    request.setState(RUNNING);

    Mockito.doThrow(BenchFlowTestIDDoesNotExistException.class)
        .when(testModelDAOMock)
        .setTestState(VALID_BENCHFLOW_TEST_ID, RUNNING);

    exception.expect(InvalidBenchFlowTestIDWebException.class);

    String[] testIDArray = VALID_BENCHFLOW_TEST_ID.split(MODEL_ID_DELIMITER_REGEX);

    String username = testIDArray[0];
    String testName = testIDArray[1];
    int testNumber = Integer.parseInt(testIDArray[2]);

    resource.changeBenchFlowTestState(username, testName, testNumber, request);
  }

  @Test
  public void getBenchFlowTestStatusInValid() throws Exception {

    String testID = INVALID_BENCHFLOW_TEST_ID;

    doThrow(BenchFlowTestIDDoesNotExistException.class).when(testModelDAOMock).getTestModel(testID);

    exception.expect(InvalidBenchFlowTestIDWebException.class);

    String[] testIDArray = INVALID_BENCHFLOW_TEST_ID.split(MODEL_ID_DELIMITER_REGEX);

    String username = testIDArray[0];
    String testName = testIDArray[1];
    int testNumber = Integer.parseInt(testIDArray[2]);

    resource.getBenchFlowTestStatus(username, testName, testNumber);

    verify(testModelDAOMock, times(1)).getTestModel(testID);
  }

  @Test
  public void getBenchFlowTestStatusValid() throws Exception {

    String benchFlowTestName = TestConstants.VALID_BENCHFLOW_TEST_NAME;

    String expectedTestID =
        TestConstants.TEST_USER_NAME
            + BenchFlowConstants.MODEL_ID_DELIMITER
            + benchFlowTestName
            + BenchFlowConstants.MODEL_ID_DELIMITER
            + 1;

    doReturn(new BenchFlowTestModel(TestConstants.TEST_USER, benchFlowTestName, 1))
        .when(testModelDAOMock)
        .getTestModel(expectedTestID);

    String[] testIDArray = expectedTestID.split(MODEL_ID_DELIMITER_REGEX);

    String username = testIDArray[0];
    String testName = testIDArray[1];
    int testNumber = Integer.parseInt(testIDArray[2]);

    BenchFlowTestModel response = resource.getBenchFlowTestStatus(username, testName, testNumber);

    verify(testModelDAOMock, times(1)).getTestModel(expectedTestID);

    // TODO - decide what status should contain and make assertions accordingly

    Assert.assertNotNull(response);
    Assert.assertEquals(expectedTestID, response.getId());
  }
}
