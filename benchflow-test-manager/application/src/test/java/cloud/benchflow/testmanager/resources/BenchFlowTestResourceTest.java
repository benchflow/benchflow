package cloud.benchflow.testmanager.resources;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.*;
import static cloud.benchflow.testmanager.helpers.constants.TestConstants.*;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.*;

import cloud.benchflow.dsl.ExplorationSpaceAPI;
import cloud.benchflow.dsl.definition.types.time.Time;
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpace;
import cloud.benchflow.testmanager.api.request.ChangeBenchFlowTestStateRequest;
import cloud.benchflow.testmanager.api.response.ChangeBenchFlowTestStateResponse;
import cloud.benchflow.testmanager.api.response.RunBenchFlowTestResponse;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.web.InvalidBenchFlowTestIDWebException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTestBundleWebException;
import cloud.benchflow.testmanager.helpers.constants.TestBundle;
import cloud.benchflow.testmanager.helpers.constants.TestFiles;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.models.explorationspace.MongoCompatibleExplorationSpace;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17.
 */
public class BenchFlowTestResourceTest {

  private static HttpServletRequest httpServletRequestMock = Mockito.mock(HttpServletRequest.class);
  @Rule
  public ExpectedException exception = ExpectedException.none();
  // needs to be subfolder of current folder for Wercker
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));
  // mocks
  private BenchFlowTestModelDAO testModelDAOMock = Mockito.mock(BenchFlowTestModelDAO.class);
  private UserDAO userDAOMock = Mockito.mock(UserDAO.class);
  private ExplorationModelDAO explorationModelDAOMock = Mockito.mock(ExplorationModelDAO.class);
  private TestTaskScheduler testTaskScheduler = Mockito.mock(TestTaskScheduler.class);
  private MinioService minioService = Mockito.mock(MinioService.class);
  private BenchFlowTestResource resource;
  private ChangeBenchFlowTestStateRequest request;

  @Before
  public void setUp() throws Exception {

    resource = new BenchFlowTestResource(testModelDAOMock, userDAOMock, explorationModelDAOMock,
        testTaskScheduler, minioService);
    request = new ChangeBenchFlowTestStateRequest();

    Mockito.doReturn("localhost").when(httpServletRequestMock).getServerName();
    Mockito.doReturn(1234).when(httpServletRequestMock).getServerPort();
  }

  @Test
  public void runBenchFlowTestEmptyRequest() throws Exception {

    exception.expect(WebApplicationException.class);
    exception.expectMessage(String.valueOf(Response.Status.BAD_REQUEST.getStatusCode()));

    resource.runBenchFlowTest(TEST_USER_NAME, null, httpServletRequestMock);
  }

  @Test
  public void runBenchFlowTestValid() throws Exception {

    InputStream testBundle = TestBundle.getLoadTestBundleInputStream(temporaryFolder);

    String expectedTestID =
        TEST_USER_NAME + MODEL_ID_DELIMITER + LOAD_TEST_NAME + MODEL_ID_DELIMITER + 1;

    Mockito.doReturn(expectedTestID).when(testModelDAOMock)
        .addTestModel(Mockito.matches(LOAD_TEST_NAME), Mockito.any(User.class));

    RunBenchFlowTestResponse response =
        resource.runBenchFlowTest(TEST_USER_NAME, testBundle, httpServletRequestMock);

    Assert.assertTrue(response.getTestID().contains(LOAD_TEST_NAME));
  }

  @Test
  public void runInvalidBenchFlowTest() throws Exception {

    InputStream testBundle = TestBundle.getMissingTestDefinitionTestBundle(temporaryFolder);

    exception.expect(InvalidTestBundleWebException.class);

    resource.runBenchFlowTest(TEST_USER_NAME, testBundle, httpServletRequestMock);
  }

  @Test
  public void changeBenchFlowTestState() throws Exception {

    Mockito.doReturn(RUNNING).when(testModelDAOMock).setTestState(VALID_TEST_ID, RUNNING);
    Mockito.doReturn(TERMINATED).when(testModelDAOMock).setTestState(VALID_TEST_ID, TERMINATED);

    request.setState(RUNNING);

    String[] testIDArray = VALID_TEST_ID.split(MODEL_ID_DELIMITER_REGEX);

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

    Mockito.doThrow(BenchFlowTestIDDoesNotExistException.class).when(testModelDAOMock)
        .setTestState(VALID_TEST_ID, RUNNING);

    exception.expect(InvalidBenchFlowTestIDWebException.class);

    String[] testIDArray = VALID_TEST_ID.split(MODEL_ID_DELIMITER_REGEX);

    String username = testIDArray[0];
    String testName = testIDArray[1];
    int testNumber = Integer.parseInt(testIDArray[2]);

    resource.changeBenchFlowTestState(username, testName, testNumber, request);
  }

  @Test
  public void getBenchFlowTestStatusInValid() throws Exception {

    String testID = INVALID_TEST_BENCHFLOW_ID;

    Mockito.doThrow(BenchFlowTestIDDoesNotExistException.class).when(testModelDAOMock)
        .getTestModel(testID);

    exception.expect(InvalidBenchFlowTestIDWebException.class);

    String[] testIDArray = INVALID_TEST_BENCHFLOW_ID.split(MODEL_ID_DELIMITER_REGEX);

    String username = testIDArray[0];
    String testName = testIDArray[1];
    int testNumber = Integer.parseInt(testIDArray[2]);

    resource.getBenchFlowTestStatus(username, testName, testNumber, httpServletRequestMock);

    Mockito.verify(testModelDAOMock, Mockito.times(1)).getTestModel(testID);
  }

  @Test
  public void getLoadTestStatusValid() throws Exception {

    // prepare the data

    String benchFlowTestName = LOAD_TEST_NAME;

    String expectedTestID = TEST_USER_NAME + BenchFlowConstants.MODEL_ID_DELIMITER
        + benchFlowTestName + BenchFlowConstants.MODEL_ID_DELIMITER + 1;

    BenchFlowTestModel testModel = new BenchFlowTestModel(TEST_USER, benchFlowTestName, 1);

    // add an experiment
    BenchFlowExperimentModel experimentModel = new BenchFlowExperimentModel(expectedTestID, 0);
    experimentModel.setExplorationPointIndex(0);

    testModel.addExperimentModel(experimentModel);

    Mockito.doReturn(testModel).when(testModelDAOMock).getTestModel(expectedTestID);

    String testDefinitionString =
        IOUtils.toString(TestFiles.getTestLoadInputStream(), StandardCharsets.UTF_8);

    JavaCompatExplorationSpace javaCompatExplorationSpace =
        ExplorationSpaceAPI.explorationSpaceFromTestYaml(testDefinitionString);

    MongoCompatibleExplorationSpace explorationSpace =
        new MongoCompatibleExplorationSpace(javaCompatExplorationSpace);

    Mockito.doReturn(explorationSpace).when(explorationModelDAOMock)
        .getExplorationSpace(expectedTestID);

    String[] testIDArray = expectedTestID.split(MODEL_ID_DELIMITER_REGEX);

    String username = testIDArray[0];
    String testName = testIDArray[1];
    int testNumber = Integer.parseInt(testIDArray[2]);

    // get the response

    BenchFlowTestModel response =
        resource.getBenchFlowTestStatus(username, testName, testNumber, httpServletRequestMock);

    // verify result is as expected

    Mockito.verify(testModelDAOMock, Mockito.times(1)).getTestModel(expectedTestID);

    // TODO - decide what status should contain and make assertions accordingly

    Assert.assertNotNull(response);
    Assert.assertEquals(expectedTestID, response.getId());

    Optional<BenchFlowExperimentModel> modelOptional =
        response.getExperimentModels().stream().findFirst();

    Assert.assertTrue(modelOptional.isPresent());

    String explorationPointURL = response.getExperimentModels().stream().findFirst().get()
        .getExplorationPointConfiguration();
    Assert.assertEquals(BenchFlowTestResource.NO_EXPLORATION_SPACE, explorationPointURL);

  }
}
