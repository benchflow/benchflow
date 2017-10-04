package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.api.request.SubmitTrialStatusRequest;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTrialIDWebException;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
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
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 13.02.17.
 */
@Path("/v1/users/{username}/tests/{testName}/{testNumber}/experiments/{experimentNumber}/trials")
@Api(value = "benchflow-trial")
public class BenchFlowTrialResource {

  public static String STATUS_PATH = "/status";

  private Logger logger = LoggerFactory.getLogger(BenchFlowTrialResource.class.getSimpleName());

  private BenchFlowExperimentModelDAO experimentModelDAO;

  public BenchFlowTrialResource() {
    this.experimentModelDAO = BenchFlowTestManagerApplication.getExperimentModelDAO();
  }

  @VisibleForTesting
  public BenchFlowTrialResource(BenchFlowExperimentModelDAO experimentModelDAO) {
    this.experimentModelDAO = experimentModelDAO;
  }

  /**
   * Updates the status of a trial. NB: currently not used by any service.
   *
   * @param username the name of the user
   * @param testName  the name of the test
   * @param testNumber  the number of the test
   * @param experimentNumber  the number of the experiment
   * @param trialNumber the number of the trial
   * @param statusRequest that status of the trial
   */
  @PUT
  @Path("/{trialNumber}/status")
  @Consumes(MediaType.APPLICATION_JSON)
  public void submitTrialStatus(@PathParam("username") String username,
      @PathParam("testName") String testName, @PathParam("testNumber") int testNumber,
      @PathParam("experimentNumber") int experimentNumber,
      @PathParam("trialNumber") int trialNumber,
      @NotNull @Valid final SubmitTrialStatusRequest statusRequest) {

    String experimentID =
        BenchFlowConstants.getExperimentID(username, testName, testNumber, experimentNumber);
    String trialID = BenchFlowConstants.getTrialID(username, testName, testNumber, experimentNumber,
        trialNumber);

    logger.info("request received: POST " + BenchFlowConstants.getPathFromTrialID(trialID)
        + STATUS_PATH + " : " + statusRequest.getStatus().name());

    try {
      experimentModelDAO.addTrialStatus(experimentID, trialNumber, statusRequest.getStatus());

    } catch (BenchFlowExperimentIDDoesNotExistException e) {
      throw new InvalidTrialIDWebException();
    }
  }
}
