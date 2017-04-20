package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.api.request.BenchFlowExperimentStateRequest;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTrialIDWebException;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-16
 */
@Path("/v1/users/{username}/tests/{testName}/{testNumber}/experiments")
@Api(value = "benchflow-experiment")
public class BenchFlowExperimentResource {

    public static String STATE_PATH = "/state";

    private Logger logger = LoggerFactory.getLogger(BenchFlowExperimentResource.class.getSimpleName());

    private BenchFlowExperimentModelDAO experimentModelDAO;

    public BenchFlowExperimentResource(BenchFlowExperimentModelDAO experimentModelDAO) {
        this.experimentModelDAO = experimentModelDAO;
    }

    @PUT
    @Path("/{experimentNumber}/state")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setExperimentState(@PathParam("username") String username,
                                   @PathParam("testName") String testName,
                                   @PathParam("testNumber") int testNumber,
                                   @PathParam("experimentNumber") int experimentNumber,
                                   @NotNull @Valid final BenchFlowExperimentStateRequest stateRequest) {

        String experimentID = BenchFlowConstants.getExperimentID(username, testName, testNumber, experimentNumber);

        logger.info("request received: POST " + BenchFlowConstants.getPathFromExperimentID(experimentID) + STATE_PATH
                + " : " + stateRequest.getState().name());

        try {
            experimentModelDAO.setExperimentState(experimentID, stateRequest.getState(), stateRequest.getStatus());
        } catch (BenchFlowExperimentIDDoesNotExistException e) {
            throw new InvalidTrialIDWebException();
        }

    }

}
