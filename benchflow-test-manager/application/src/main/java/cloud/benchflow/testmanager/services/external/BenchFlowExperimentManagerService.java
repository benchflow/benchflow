package cloud.benchflow.testmanager.services.external;

import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;


/**
 * uses Jersey Client:
 * http://www.dropwizard.io/1.0.6/docs/manual/client.html
 * https://jersey.java.net/documentation/2.22.1/client.html
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 18.12.16.
 */
public class BenchFlowExperimentManagerService {

    // TODO - move this to common library?
    private static String RUN_PATH = "/run";
    private static String STATE_PE_PATH = "/state";

    private Logger logger = LoggerFactory.getLogger(BenchFlowExperimentManagerService.class.getSimpleName());

    private WebTarget experimentManagerTarget;

    public BenchFlowExperimentManagerService(Client httpClient, String experimentManagerAddress) {

        this.experimentManagerTarget = httpClient.target("http://" + experimentManagerAddress);
    }

    public void runBenchFlowExperiment(String experimentID) {

        logger.info("runBenchFlowExperiment: " + experimentID);

        Response runPEResponse = experimentManagerTarget
                .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
                .path(RUN_PATH)
                .request()
                .post(null);

        if (runPEResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {

            // TODO - handle possible errors and throw exceptions accordingly
            logger.error("runBenchFlowExperiment: error connecting - " + runPEResponse.getStatus());

        } else {
            logger.info("runBenchFlowExperiment: connected successfully");
        }

    }

    public BenchFlowExperimentModel.BenchFlowExperimentState abortBenchFlowExperiment(String experimentID) {

        logger.info("abortBenchFlowExperiment: " + experimentID);

        BenchFlowExperimentStateEntity stateEntity = new BenchFlowExperimentStateEntity(
                BenchFlowExperimentModel.BenchFlowExperimentState.ABORTED);

        Response abortPEResponse = experimentManagerTarget
                .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
                .path(STATE_PE_PATH)
                .request().post(Entity.entity(stateEntity, MediaType.APPLICATION_JSON));

        if (abortPEResponse.getStatus() != Response.Status.OK.getStatusCode()) {

            // TODO - handle possible errors and throw exceptions accordingly

            logger.error("abortBenchFlowExperiment: error connecting - " + abortPEResponse.getStatus());
        }

        BenchFlowExperimentStateEntity responseStateEntity = abortPEResponse.readEntity(BenchFlowExperimentStateEntity.class);

        return responseStateEntity.getState();
    }

    // TODO - move this to common library?
    private class BenchFlowExperimentStateEntity {

        private BenchFlowExperimentModel.BenchFlowExperimentState state;

        public BenchFlowExperimentStateEntity(BenchFlowExperimentModel.BenchFlowExperimentState state) {
            this.state = state;
        }

        public BenchFlowExperimentModel.BenchFlowExperimentState getState() {
            return state;
        }

        public void setState(BenchFlowExperimentModel.BenchFlowExperimentState state) {
            this.state = state;
        }
    }


}
