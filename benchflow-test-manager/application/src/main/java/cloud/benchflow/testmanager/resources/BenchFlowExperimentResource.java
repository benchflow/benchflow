package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.web.InvalidBenchFlowTestIDWebException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTrialIDWebException;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-16
 */
@Path("/v1/users/{username}/tests/{testName}/{testNumber}/experiments")
@Api(value = "benchflow-experiment")
public class BenchFlowExperimentResource {

  public static String STATE_PATH = "/state";

  private static Logger logger =
      LoggerFactory.getLogger(BenchFlowExperimentResource.class.getSimpleName());

  private final BenchFlowExperimentModelDAO experimentModelDAO;
  private final BenchFlowTestModelDAO testModelDAO;
  private final TestTaskScheduler testTaskController;

  public BenchFlowExperimentResource() {
    this.testTaskController = BenchFlowTestManagerApplication.getTestTaskScheduler();
    this.experimentModelDAO = BenchFlowTestManagerApplication.getExperimentModelDAO();
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
  }

  /* used for testing */
  public BenchFlowExperimentResource(BenchFlowExperimentModelDAO experimentModelDAO,
      TestTaskScheduler testTaskController, BenchFlowTestModelDAO testModelDAO) {
    this.experimentModelDAO = experimentModelDAO;
    this.testTaskController = testTaskController;
    this.testModelDAO = testModelDAO;
  }

  @PUT
  @Path("/{experimentNumber}/state")
  @Consumes(MediaType.APPLICATION_JSON)
  public void setExperimentState(@PathParam("username") String username,
      @PathParam("testName") String testName, @PathParam("testNumber") int testNumber,
      @PathParam("experimentNumber") int experimentNumber,
      @NotNull @Valid final BenchFlowExperimentStateRequest stateRequest) {

    String experimentID =
        BenchFlowConstants.getExperimentID(username, testName, testNumber, experimentNumber);

    logger.info("request received: POST " + BenchFlowConstants.getPathFromExperimentID(experimentID)
        + STATE_PATH + " : " + stateRequest.getState().name());

    String testID = BenchFlowConstants.getTestIDFromExperimentID(experimentID);

    try {

      BenchFlowTestModel.BenchFlowTestState testState = testModelDAO.getTestState(testID);

      if (!testState.equals(BenchFlowTestModel.BenchFlowTestState.RUNNING)) {
        throw new WebApplicationException("test not running");
      }

    } catch (BenchFlowTestIDDoesNotExistException e) {
      throw new InvalidBenchFlowTestIDWebException();
    }

    try {
      experimentModelDAO.setExperimentState(experimentID, stateRequest.getState(),
          stateRequest.getRunningState(), stateRequest.getTerminatedState());
    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      throw new InvalidTrialIDWebException();
    }

    if (stateRequest.getState()
        .equals(BenchFlowExperimentModel.BenchFlowExperimentState.TERMINATED)) {

      testTaskController.handleRunningTest(testID);
    }

    // for now we ignore other states since we are only concerned if the experiment has terminated

  }
}
