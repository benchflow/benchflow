package cloud.benchflow.testmanager.resources;

import cloud.benchflow.dsl.BenchFlowTestAPI;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationExceptionMessage;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.api.response.GetUserTestsResponse;
import cloud.benchflow.testmanager.api.response.RunBenchFlowTestResponse;
import cloud.benchflow.testmanager.bundle.BenchFlowTestBundleExtractor;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.InvalidTestBundleException;
import cloud.benchflow.testmanager.exceptions.UserIDAlreadyExistsException;
import cloud.benchflow.testmanager.exceptions.web.InvalidBenchFlowTestIDWebException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTestBundleWebException;
import cloud.benchflow.testmanager.exceptions.web.InvalidUsernameWebException;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.models.explorationspace.MongoCompatibleExplorationSpace;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import io.swagger.annotations.Api;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 13.02.17.
 */
@Path("/v1/users/{username}/tests")
@Api(value = "benchflow-test")
public class BenchFlowTestResource {

  public static String TEST_PATH = "/tests";
  public static String RUN_PATH = "/run";
  public static String STATUS_PATH = "/status";
  public static String ABORT_PATH = "/abort";
  public static String NO_EXPLORATION_SPACE = "no exploration space";
  private final BenchFlowTestModelDAO testModelDAO;
  private final UserDAO userDAO;
  private final ExplorationModelDAO explorationModelDAO;
  private final TestTaskScheduler testTaskScheduler;
  private final MinioService minioService;
  private Logger logger = LoggerFactory.getLogger(BenchFlowTestResource.class.getSimpleName());


  public BenchFlowTestResource() {
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
    this.userDAO = BenchFlowTestManagerApplication.getUserDAO();
    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    this.testTaskScheduler = BenchFlowTestManagerApplication.getTestTaskScheduler();
    this.minioService = BenchFlowTestManagerApplication.getMinioService();
  }

  /* used for tests */
  public BenchFlowTestResource(BenchFlowTestModelDAO testModelDAO, UserDAO userDAO,
      ExplorationModelDAO explorationModelDAO, TestTaskScheduler testTaskScheduler,
      MinioService minioService) {
    this.testModelDAO = testModelDAO;
    this.userDAO = userDAO;
    this.explorationModelDAO = explorationModelDAO;
    this.testTaskScheduler = testTaskScheduler;
    this.minioService = minioService;
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public GetUserTestsResponse getUserTests(@PathParam("username") String username) {

    logger.info(
        "request received: GET " + BenchFlowConstants.getPathFromUsername(username) + TEST_PATH);

    User user = userDAO.getUser(username);

    if (user == null) {
      throw new InvalidUsernameWebException();
    }

    List testIDsRaw = testModelDAO.getUserTestModels(user);

    List<String> testIDs = new ArrayList<>();

    for (Object o : testIDsRaw) {
      testIDs.add(o.toString());
    }

    GetUserTestsResponse userTestsResponse = new GetUserTestsResponse();
    userTestsResponse.setTestIDs(testIDs);

    return userTestsResponse;

  }

  @POST
  @Path("/run")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public RunBenchFlowTestResponse runBenchFlowTest(@PathParam("username") String username,
      @FormDataParam("benchFlowTestBundle") final InputStream benchFlowTestBundle,
      @Context HttpServletRequest request) {

    logger.info("request received: POST " + BenchFlowConstants.getPathFromUsername(username)
        + TEST_PATH + RUN_PATH);

    if (benchFlowTestBundle == null) {
      logger.info("runBenchFlowTest: test bundle == null");
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    ZipInputStream testBundleZipInputStream = new ZipInputStream(benchFlowTestBundle);

    User user = new User(username);

    // TODO - check valid user
    // TODO - move user creating into separate Class for handling users
    if (!userDAO.userExists(user)) {

      try {
        userDAO.addUser(user.getUsername());
      } catch (UserIDAlreadyExistsException e) {
        // since we already checked that the user doesn't exist it cannot happen
        logger.info("runBenchFlowTest: user doesn't exist");
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }

    try {

      // validate test bundle
      // Get the contents of bundle and check if valid Test ID
      String testDefinitionYamlString = BenchFlowTestBundleExtractor
          .extractBenchFlowTestDefinitionString(testBundleZipInputStream);

      if (testDefinitionYamlString == null) {
        logger.info("runBenchFlowTest: testDefinitionYamlString == null");
        throw new InvalidTestBundleException();
      }

      BenchFlowTest benchFlowTest = BenchFlowTestAPI.testFromYaml(testDefinitionYamlString);

      InputStream deploymentDescriptorInputStream = BenchFlowTestBundleExtractor
          .extractDeploymentDescriptorInputStream(testBundleZipInputStream);
      Map<String, InputStream> bpmnModelInputStreams =
          BenchFlowTestBundleExtractor.extractBPMNModelInputStreams(testBundleZipInputStream);

      if (deploymentDescriptorInputStream == null || bpmnModelInputStreams.size() == 0) {
        logger.info("runBenchFlowTest: deploymentDescriptorInputStream == null "
            + "|| bpmnModelInputStreams.size() == 0");
        throw new InvalidTestBundleException();
      }

      // save new test
      String testID = testModelDAO.addTestModel(benchFlowTest.name(), user);

      // save files in separate thread to return faster to user
      new Thread(() -> {

        // extract contents
        InputStream definitionInputStream =
            IOUtils.toInputStream(testDefinitionYamlString, StandardCharsets.UTF_8);

        // save Test Bundle contents to Minio
        minioService.saveTestDefinition(testID, definitionInputStream);
        minioService.saveTestDeploymentDescriptor(testID, deploymentDescriptorInputStream);

        bpmnModelInputStreams.forEach((fileName, inputStream) -> minioService
            .saveTestBPMNModel(testID, fileName, inputStream));

        // delegate to task scheduler
        testTaskScheduler.handleStartingTest(testID);

      }).start();

      String statusURL = "http://" + request.getServerName() + ":" + request.getServerPort()
          + BenchFlowConstants.getPathFromTestID(testID) + STATUS_PATH;

      return new RunBenchFlowTestResponse(testID, statusURL);

    } catch (IOException | InvalidTestBundleException | BenchFlowDeserializationException
        | BenchFlowDeserializationExceptionMessage e) {
      // TODO - throw more fine grained errors, e.g., file missing in bundle, deserialization error
      logger.error(e.getClass().getSimpleName());
      if (e.getMessage() == null) {
        // if no message we only throw the exception
        throw new InvalidTestBundleWebException();
      }

      logger.error(e.getMessage());
      throw new InvalidTestBundleWebException(e.getMessage());
    }
  }

  @Path("{testName}/{testNumber}/status")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public BenchFlowTestModel getBenchFlowTestStatus(@PathParam("username") String username,
      @PathParam("testName") String testName, @PathParam("testNumber") int testNumber,
      @Context HttpServletRequest request) {

    String testID = BenchFlowConstants.getTestID(username, testName, testNumber);

    logger.info(
        "request received: GET " + BenchFlowConstants.getPathFromTestID(testID) + STATUS_PATH);

    // get the BenchFlowTestModel from DAO
    BenchFlowTestModel benchFlowTestModel = null;

    try {

      benchFlowTestModel = testModelDAO.getTestModel(testID);

      // set the URL to the exploration point index
      // it is done dynamically since we need the server information
      for (BenchFlowExperimentModel experimentModel : benchFlowTestModel.getExperimentModels()) {

        int pointIndex = experimentModel.getExplorationPointIndex();

        // add url only if exploration space is not empty or null

        MongoCompatibleExplorationSpace explorationSpace =
            explorationModelDAO.getExplorationSpace(testID);

        String url;

        if (explorationSpace == null || explorationSpace.getSize() == 0) {

          url = NO_EXPLORATION_SPACE;


        } else {

          url = "http://" + request.getServerName() + ":" + request.getServerPort()
              + BenchFlowConstants.getPathFromTestID(testID)
              + ExplorationPointResource.EXPLORATION_POINT_PATH + pointIndex;

        }

        experimentModel.setExplorationPointConfiguration(url);

      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      throw new InvalidBenchFlowTestIDWebException();
    }

    return benchFlowTestModel;
  }

  @POST
  @Path("{testName}/{testNumber}/abort")
  public void abortBenchFlowTest(@PathParam("username") String username,
      @PathParam("testName") String testName, @PathParam("testNumber") int testNumber) {

    String testID = BenchFlowConstants.getTestID(username, testName, testNumber);
    logger.info(
        "request received: POST " + BenchFlowConstants.getPathFromTestID(testID) + ABORT_PATH);

    if (testModelDAO.testModelExists(testID)) {

      testTaskScheduler.terminateTest(testID);

    } else {
      logger.info("abortBenchFlowTest: invalid test id - " + testID);
      throw new InvalidBenchFlowTestIDWebException();

    }

  }
}
