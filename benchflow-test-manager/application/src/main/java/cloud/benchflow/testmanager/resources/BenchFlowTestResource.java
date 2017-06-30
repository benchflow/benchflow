package cloud.benchflow.testmanager.resources;

import cloud.benchflow.dsl.BenchFlowTestAPI;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.api.request.ChangeBenchFlowTestStateRequest;
import cloud.benchflow.testmanager.api.response.ChangeBenchFlowTestStateResponse;
import cloud.benchflow.testmanager.api.response.RunBenchFlowTestResponse;
import cloud.benchflow.testmanager.bundle.BenchFlowTestBundleExtractor;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.InvalidTestBundleException;
import cloud.benchflow.testmanager.exceptions.UserIDAlreadyExistsException;
import cloud.benchflow.testmanager.exceptions.web.InvalidBenchFlowTestIDWebException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTestBundleWebException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import io.swagger.annotations.Api;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

  public static String RUN_PATH = "/run";
  public static String STATE_PATH = "/state";
  public static String STATUS_PATH = "/status";
  private final BenchFlowTestModelDAO testModelDAO;
  private final UserDAO userDAO;
  private final TestTaskScheduler testTaskScheduler;
  private final MinioService minioService;
  private Logger logger = LoggerFactory.getLogger(BenchFlowTestResource.class.getSimpleName());


  public BenchFlowTestResource() {
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
    this.userDAO = BenchFlowTestManagerApplication.getUserDAO();
    this.testTaskScheduler = BenchFlowTestManagerApplication.getTestTaskScheduler();
    this.minioService = BenchFlowTestManagerApplication.getMinioService();
  }

  /* used for tests */
  public BenchFlowTestResource(BenchFlowTestModelDAO testModelDAO, UserDAO userDAO,
      TestTaskScheduler testTaskScheduler, MinioService minioService) {
    this.testModelDAO = testModelDAO;
    this.userDAO = userDAO;
    this.testTaskScheduler = testTaskScheduler;
    this.minioService = minioService;
  }

  @POST
  @Path("/run")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public RunBenchFlowTestResponse runBenchFlowTest(@PathParam("username") String username,
      @FormDataParam("benchFlowTestBundle") final InputStream benchFlowTestBundle,
      @Context HttpServletRequest request) {

    logger.info(
        "request received: POST " + BenchFlowConstants.getPathFromUsername(username) + RUN_PATH);

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
        logger.info(
            "runBenchFlowTest: deploymentDescriptorInputStream == null || bpmnModelInputStreams.size() == 0");
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
        testTaskScheduler.handleTestState(testID);

      }).start();

      String statusURL = "http://" + request.getServerName() + ":" + request.getServerPort()
          + BenchFlowConstants.getPathFromTestID(testID) + STATUS_PATH;

      return new RunBenchFlowTestResponse(testID, statusURL);

    } catch (IOException | InvalidTestBundleException | BenchFlowDeserializationException e) {
      // TODO - throw more fine grained errors, e.g., file missing in bundle, deserialization error
      throw new InvalidTestBundleWebException();
    }
  }

  @PUT
  @Path("{testName}/{testNumber}/state")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public ChangeBenchFlowTestStateResponse changeBenchFlowTestState(
      @PathParam("username") String username, @PathParam("testName") String testName,
      @PathParam("testNumber") int testNumber,
      @NotNull @Valid final ChangeBenchFlowTestStateRequest stateRequest) {

    String testID = BenchFlowConstants.getTestID(username, testName, testNumber);
    logger
        .info("request received: PUT " + BenchFlowConstants.getPathFromTestID(testID) + STATE_PATH);

    // TODO - handle the actual state change (e.g. on Experiment Manager)

    // update the state
    BenchFlowTestModel.BenchFlowTestState newState = null;

    try {
      newState = testModelDAO.setTestState(testID, stateRequest.getState());
    } catch (BenchFlowTestIDDoesNotExistException e) {
      throw new InvalidBenchFlowTestIDWebException();
    }

    // return the state as saved
    return new ChangeBenchFlowTestStateResponse(newState);
  }

  @Path("{testName}/{testNumber}/status")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public BenchFlowTestModel getBenchFlowTestStatus(@PathParam("username") String username,
      @PathParam("testName") String testName, @PathParam("testNumber") int testNumber) {

    String testID = BenchFlowConstants.getTestID(username, testName, testNumber);

    logger.info(
        "request received: GET " + BenchFlowConstants.getPathFromTestID(testID) + STATUS_PATH);

    // get the BenchFlowTestModel from DAO
    BenchFlowTestModel benchFlowTestModel = null;

    try {
      benchFlowTestModel = testModelDAO.getTestModel(testID);
    } catch (BenchFlowTestIDDoesNotExistException e) {
      throw new InvalidBenchFlowTestIDWebException();
    }

    return benchFlowTestModel;
  }
}
