package cloud.benchflow.experimentmanager.resources;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.api.request.FabanStatusRequest;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.web.NoSuchExperimentIdException;
import cloud.benchflow.experimentmanager.scheduler.ExperimentTaskScheduler;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-08-19
 */
@Path("/v1/users/{username}/tests/{testName}/{testNumber}/experiments/{experimentNumber}/trials/{trialNumber}")
@Api(value = "benchflow-trial")
public class TrialResource {

  public static final String RESULT_PATH = "/result";

  private static Logger logger = LoggerFactory.getLogger(TrialResource.class.getSimpleName());

  private TrialModelDAO trialModelDAO;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private ExperimentTaskScheduler experimentTaskScheduler;

  public TrialResource() {
    this.trialModelDAO = BenchFlowExperimentManagerApplication.getTrialModelDAO();
    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.experimentTaskScheduler =
        BenchFlowExperimentManagerApplication.getExperimentTaskScheduler();
  }


  @PUT
  @Path("/result")
  public void setFabanResult(@PathParam("username") String username,
      @PathParam("testName") String testName, @PathParam("testNumber") int testNumber,
      @PathParam("experimentNumber") int experimentNumber,
      @PathParam("trialNumber") int trialNumber,
      @NotNull @Valid FabanStatusRequest fabanStatusRequest) {

    String experimentID =
        BenchFlowConstants.getExperimentID(username, testName, testNumber, experimentNumber);

    String trialID = BenchFlowConstants.getTrialID(experimentID, trialNumber);

    logger.info(
        "request received: POST " + BenchFlowConstants.getPathFromTrialID(trialID) + RESULT_PATH);

    // check that experiment exists
    if (!experimentModelDAO.experimentExists(experimentID)) {
      throw new NoSuchExperimentIdException(experimentID);
    }

    trialModelDAO.setFabanStatus(trialID, fabanStatusRequest.getStatusCode());
    trialModelDAO.setFabanResult(trialID, fabanStatusRequest.getResult());

    // continue handling in task scheduler
    new Thread(() -> experimentTaskScheduler.handleRunningExperiment(experimentID)).start();

  }

}
