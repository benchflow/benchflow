package cloud.benchflow.experimentmanager.services.external;

import cloud.benchflow.experimentmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.experimentmanager.api.request.SubmitTrialStatusRequest;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.faban.client.responses.RunStatus;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 05.03.17.
 */
public class BenchFlowTestManagerService {

  public static final String TRIAL_STATUS_PATH = "/status";
  public static final String EXPERIMENT_STATE_PATH = "/state";

  private Logger logger =
      LoggerFactory.getLogger(BenchFlowTestManagerService.class.getSimpleName());

  private WebTarget testManagerTarget;

  public BenchFlowTestManagerService(Client httpClient, String testManagerAddress) {

    this.testManagerTarget = httpClient.target("http://" + testManagerAddress);
  }

  public void submitTrialStatus(String trialID, RunStatus.StatusCode statusCode) {

    logger.info("submitTrialStatus for " + trialID + " with status " + statusCode.name());

    SubmitTrialStatusRequest trialStatusRequest = new SubmitTrialStatusRequest();
    trialStatusRequest.setStatus(statusCode);

    Response response = testManagerTarget.path(BenchFlowConstants.getPathFromTrialID(trialID))
        .path(TRIAL_STATUS_PATH).request()
        .put(Entity.entity(trialStatusRequest, MediaType.APPLICATION_JSON));

    if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {

      logger.error("submitTrialStatus: error connecting - " + response.getStatus());

    } else {
      logger.info("submitTrialStatus: successfully connected");
    }
  }

  public void setExperimentRunningState(String experimentID, RunningState runningState) {

    logger.info(
        "setExperimentRunningState for " + experimentID + " runningState: " + runningState.name());

    BenchFlowExperimentStateRequest stateRequest =
        new BenchFlowExperimentStateRequest(BenchFlowExperimentState.RUNNING, runningState);

    setExperimentState(experimentID, stateRequest);
  }

  public void setExperimentTerminatedState(String experimentID,
      BenchFlowExperimentModel.TerminatedState terminatedState) {

    logger.info("setExperimentRunningState for " + experimentID + " terminatedState: "
        + terminatedState.name());

    BenchFlowExperimentStateRequest stateRequest =
        new BenchFlowExperimentStateRequest(BenchFlowExperimentState.TERMINATED, terminatedState);

    setExperimentState(experimentID, stateRequest);
  }

  private void setExperimentState(String experimentID,
      BenchFlowExperimentStateRequest stateRequest) {

    Response response = testManagerTarget
        .path(BenchFlowConstants.getPathFromExperimentID(experimentID)).path(EXPERIMENT_STATE_PATH)
        .request().put(Entity.entity(stateRequest, MediaType.APPLICATION_JSON));

    if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
      logger.error("setExperimentRunningState: error connecting - " + response.getStatus());
    } else {
      logger.info("setExperimentRunningState: successfully connected");
    }
  }
}
