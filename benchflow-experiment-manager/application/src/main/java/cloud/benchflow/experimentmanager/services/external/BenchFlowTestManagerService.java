package cloud.benchflow.experimentmanager.services.external;

import cloud.benchflow.experimentmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.experimentmanager.api.request.BenchFlowExperimentStatusRequest;
import cloud.benchflow.experimentmanager.api.request.SubmitTrialStatusRequest;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.faban.client.responses.RunStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    public static final String EXPERIMENT_RUNNING_PATH = "/running";

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

    public void setExperimentAsTerminated(String experimentID, BenchFlowExperimentModel.BenchFlowExperimentStatus status) {

        logger.info("setExperimentAsTerminated for " + experimentID + " with status " + status.name());

        BenchFlowExperimentStatusRequest terminatedRequest = new BenchFlowExperimentStatusRequest(status);

        // TODO - adjust path accordingly

        Response response = testManagerTarget
                .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
                .path(EXPERIMENT_STATE_PATH)
                .request()
                .put(Entity.entity(terminatedRequest, MediaType.APPLICATION_JSON));

        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {

            logger.error("setExperimentAsTerminated: error connecting - " + response.getStatus());

        } else {
            logger.info("setExperimentAsTerminated: successfully connected");
        }

    }

    public void setExperimentAsRunning(String experimentID) {

        logger.info("setExperimentAsRunning for " + experimentID);


        // TODO - seems it is not possible to have a put with null
        Response response = testManagerTarget
                .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
                .path(EXPERIMENT_RUNNING_PATH)
                .request()
                .post(null);

        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {

            logger.error("setExperimentAsRunning: error connecting - " + response.getStatus());

        } else {
            logger.info("setExperimentAsRunning: successfully connected");
        }

    }

}
