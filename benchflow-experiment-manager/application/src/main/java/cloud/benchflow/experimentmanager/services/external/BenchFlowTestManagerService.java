package cloud.benchflow.experimentmanager.services.external;

import cloud.benchflow.experimentmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.experimentmanager.api.request.SubmitTrialStatusRequest;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentStatus;
import cloud.benchflow.faban.client.responses.RunStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 05.03.17.
 */
public class BenchFlowTestManagerService {

    public static final String TRIAL_STATUS_PATH = "/status";
    public static final String EXPERIMENT_STATE_PATH = "/state";

    private Logger logger = LoggerFactory.getLogger(BenchFlowTestManagerService.class.getSimpleName());

    private WebTarget testManagerTarget;

    public BenchFlowTestManagerService(Client httpClient, String testManagerAddress) {

        this.testManagerTarget = httpClient.target("http://" + testManagerAddress);
    }

    public void submitTrialStatus(String trialID, RunStatus.Code statusCode) {

        logger.info("submitTrialStatus for " + trialID + " with status " + statusCode.name());

        SubmitTrialStatusRequest trialStatusRequest = new SubmitTrialStatusRequest();
        trialStatusRequest.setStatus(statusCode);

        Response response = testManagerTarget
                .path(BenchFlowConstants.getPathFromTrialID(trialID))
                .path(TRIAL_STATUS_PATH)
                .request()
                .put(Entity.entity(trialStatusRequest, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {

            logger.error("submitTrialStatus: error connecting - " + response.getStatus());

        } else {
            logger.info("submitTrialStatus: successfully connected");
        }


    }

    public void setExperimentState(String experimentID, BenchFlowExperimentState state, BenchFlowExperimentStatus status) {

        logger.info("setExperimentState for " + experimentID + " state: " + state.name() + " status: " + status.name());

        BenchFlowExperimentStateRequest terminatedRequest = new BenchFlowExperimentStateRequest(state, status);

        Response response = testManagerTarget
                .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
                .path(EXPERIMENT_STATE_PATH)
                .request()
                .put(Entity.entity(terminatedRequest, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            logger.error("setExperimentState: error connecting - " + response.getStatus());
        } else {
            logger.info("setExperimentState: successfully connected");
        }

    }

}
