package cloud.benchflow.experimentmanager.resources;

import cloud.benchflow.experimentmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.experimentmanager.api.response.BenchFlowExperimentStateResponse;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.tasks.RunBenchFlowExperimentTask;
import cloud.benchflow.faban.client.FabanClient;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutorService;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *         <p>
 *         Created on 05/03/16.
 */
@Path("/v1/users/{username}/tests/{testName}/{testNumber}/experiments/{experimentNumber}")
@Api(value = "benchflow-experiment")
public class BenchFlowExperimentResource {

    public static final String RUN_ACTION_PATH = "/run";
    public static final String ACTION_PATH = "/state";

    private static Logger logger = LoggerFactory.getLogger(BenchFlowExperimentResource.class.getSimpleName());

    private MinioService minio;
    private BenchFlowExperimentModelDAO experimentModelDAO;
    private FabanClient faban;
    private DriversMakerService driversMaker;
    private BenchFlowTestManagerService testManagerService;
    private ExecutorService taskExecutorService;

    private int submitRetries;

    public BenchFlowExperimentResource(
            MinioService minio,
            BenchFlowExperimentModelDAO experimentModelDAO,
            FabanClient faban,
            DriversMakerService driversMaker,
            ExecutorService taskExecutorService,
            BenchFlowTestManagerService testManagerService,
            int submitRetries
    ) {
        this.minio = minio;
        this.experimentModelDAO = experimentModelDAO;
        this.faban = faban;
        this.driversMaker = driversMaker;
        this.taskExecutorService = taskExecutorService;
        this.testManagerService = testManagerService;
        this.submitRetries = submitRetries;
    }

    @POST
    @Path("/run")
    public void runBenchFlowExperiment(@PathParam("username") String username,
                                       @PathParam("testName") String testName,
                                       @PathParam("testNumber") int testNumber,
                                       @PathParam("experimentNumber") int experimentNumber) {

        String experimentID = BenchFlowConstants.getExperimentID(username, testName, testNumber, experimentNumber);

        logger.info("request received: POST " + BenchFlowConstants.getPathFromExperimentID(experimentID) + RUN_ACTION_PATH);


        // check that the experiment exists
        if (!minio.isValidExperimentID(experimentID)) {
            logger.info("invalid experimentID: " + experimentID);
            throw new WebApplicationException(BenchFlowConstants.INVALID_EXPERIMENT_ID_MESSAGE, Response.Status.PRECONDITION_FAILED);
        }

        RunBenchFlowExperimentTask task = new RunBenchFlowExperimentTask(
                experimentID,
                experimentModelDAO,
                minio,
                faban,
                driversMaker,
                testManagerService,
                submitRetries
        );

        // TODO - should go into a stateless queue (so that we can recover)
        // (for now) only allows one experiment at a time (poolSize == 1)
        taskExecutorService.submit(task);

    }

    @GET
    @Path("/state")
    @Produces(MediaType.APPLICATION_JSON)
    public BenchFlowExperimentStateResponse getExperimentState(@PathParam("username") String username,
                                                               @PathParam("testName") String testName,
                                                               @PathParam("testNumber") int testNumber,
                                                               @PathParam("experimentNumber") int experimentNumber) {

        String experimentID = BenchFlowConstants.getExperimentID(username, testName, testNumber, experimentNumber);

        logger.info("GET " + BenchFlowConstants.getPathFromExperimentID(experimentID) + ACTION_PATH);

        try {

            BenchFlowExperimentModel.BenchFlowExperimentState state = experimentModelDAO.getExperimentModelState(experimentID);

            return new BenchFlowExperimentStateResponse(state);

        } catch (BenchFlowExperimentIDDoesNotExistException e) {
            logger.error(BenchFlowConstants.INVALID_EXPERIMENT_ID_MESSAGE + ": " + experimentID + ": " + e.getMessage());
            throw new WebApplicationException(BenchFlowConstants.INVALID_EXPERIMENT_ID_MESSAGE);
        }

    }

    @PUT
    @Path("/state")
    @Consumes(MediaType.APPLICATION_JSON)
    public BenchFlowExperimentStateResponse changeExperimentState(@PathParam("username") String username,
                                                                  @PathParam("testName") String testName,
                                                                  @PathParam("testNumber") int testNumber,
                                                                  @PathParam("experimentNumber") int experimentNumber,
                                                                  @NotNull @Valid BenchFlowExperimentStateRequest stateRequest) {

        logger.info("PUT /" + username + "/" + testName + "/" + testNumber + "/" + experimentNumber + ACTION_PATH);

        String experimentID = BenchFlowConstants.getExperimentID(username, testName, testNumber, experimentNumber);

        BenchFlowExperimentModel.BenchFlowExperimentState state = experimentModelDAO.setExperimentModelState(
                experimentID,
                stateRequest.getState()
        );

        if (state == null) {
            logger.info(BenchFlowConstants.INVALID_EXPERIMENT_ID_MESSAGE + ": " + experimentID);
            throw new WebApplicationException(BenchFlowConstants.INVALID_EXPERIMENT_ID_MESSAGE);
        }

        return new BenchFlowExperimentStateResponse(state);

    }

}
