package cloud.benchflow.experimentmanager.services.external;

import cloud.benchflow.experimentmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.faban.client.responses.RunStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
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

    public void submitExperimentState(String experimentID, BenchFlowExperimentModel.BenchFlowExperimentState state) {

        logger.info("submitExperimentState for " + experimentID + " with status " + state.name());

        BenchFlowExperimentStateRequest experimentStateRequest = new BenchFlowExperimentStateRequest(state);

        Response response = testManagerTarget
                .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
                .path(EXPERIMENT_STATE_PATH)
                .request()
                .put(Entity.entity(experimentStateRequest, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {

            logger.error("submitExperimentState: error connecting - " + response.getStatus());

        } else {
            logger.info("submitExperimentState: successfully connected");
        }

    }

    private class SubmitTrialStatusRequest {

        @NotNull
        @JsonProperty
        private RunStatus.Code status;

        public SubmitTrialStatusRequest() {
        }

        public SubmitTrialStatusRequest(RunStatus.Code status) {
            this.status = status;
        }

        public RunStatus.Code getStatus() {
            return status;
        }

        public void setStatus(RunStatus.Code status) {
            this.status = status;
        }
    }
}
