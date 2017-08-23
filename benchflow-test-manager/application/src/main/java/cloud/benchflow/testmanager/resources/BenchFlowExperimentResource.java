package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.web.InvalidBenchFlowTestIDWebException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTrialIDWebException;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import com.google.common.annotations.VisibleForTesting;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
  private TestTaskScheduler testTaskScheduler;

  public BenchFlowExperimentResource() {
    this.testTaskScheduler = BenchFlowTestManagerApplication.getTestTaskScheduler();
    this.experimentModelDAO = BenchFlowTestManagerApplication.getExperimentModelDAO();
    this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
  }

  @VisibleForTesting
  public BenchFlowExperimentResource(BenchFlowExperimentModelDAO experimentModelDAO,
      TestTaskScheduler testTaskScheduler, BenchFlowTestModelDAO testModelDAO) {
    this.experimentModelDAO = experimentModelDAO;
    this.testTaskScheduler = testTaskScheduler;
    this.testModelDAO = testModelDAO;
  }

  @VisibleForTesting
  public void setTestTaskScheduler(TestTaskScheduler testTaskScheduler) {
    this.testTaskScheduler = testTaskScheduler;
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

      // we update the experiment state also in the case that the test has terminated in order
      // to be synchronized with the state on the experiment manager
      experimentModelDAO.setExperimentState(experimentID, stateRequest.getState(),
          stateRequest.getRunningState(), stateRequest.getTerminatedState());

      BenchFlowExperimentModel.BenchFlowExperimentState experimentState = stateRequest.getState();

      // if the test has not terminated (e.g is running or terminating) and the experiment state is terminated
      // we inform the testTaskController

      if (testState != BenchFlowTestState.TERMINATED
          && experimentState == BenchFlowExperimentState.TERMINATED) {

        new Thread(() -> testTaskScheduler.handleRunningTest(testID)).start();

      }

      // for now we ignore other states since we are only concerned if the experiment has terminated

    } catch (BenchFlowTestIDDoesNotExistException e) {
      throw new InvalidBenchFlowTestIDWebException();
    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      throw new InvalidTrialIDWebException();
    }


  }
}
