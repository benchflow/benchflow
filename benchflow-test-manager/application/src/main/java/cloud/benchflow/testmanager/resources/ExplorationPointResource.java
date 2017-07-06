package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.api.response.ExplorationSpacePointResponse;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.web.InvalidBenchFlowTestIDWebException;
import cloud.benchflow.testmanager.models.explorationspace.MongoCompatibleExplorationSpace;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import io.swagger.annotations.Api;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-07-05
 */
@Path("/v1/users/{username}/tests/{testName}/{testNumber}/exploration-points")
@Api(value = "exploration-point")
public class ExplorationPointResource {

  public static String EXPLORATION_POINT_PATH = "/exploration-points/";

  private static Logger logger =
      LoggerFactory.getLogger(ExplorationPointResource.class.getSimpleName());

  private ExplorationModelDAO explorationModelDAO;

  public ExplorationPointResource() {

    this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
  }

  // used for testing
  public ExplorationPointResource(ExplorationModelDAO explorationModelDAO) {
    this.explorationModelDAO = explorationModelDAO;
  }

  @Path("/{explorationPointIndex}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ExplorationSpacePointResponse getBenchFlowTestStatus(
      @PathParam("username") String username, @PathParam("testName") String testName,
      @PathParam("testNumber") int testNumber,
      @PathParam("explorationPointIndex") int explorationPointIndex) {

    String testID = BenchFlowConstants.getTestID(username, testName, testNumber);

    logger.info("request received: GET " + BenchFlowConstants.getPathFromTestID(testID)
        + EXPLORATION_POINT_PATH + explorationPointIndex);

    try {

      MongoCompatibleExplorationSpace mongoCompatibleExplorationSpace =
          explorationModelDAO.getExplorationSpace(testID);

      return mongoCompatibleExplorationSpace
          .getExplorationSpacePointResponse(explorationPointIndex);

    } catch (BenchFlowTestIDDoesNotExistException e) {
      throw new InvalidBenchFlowTestIDWebException();
    }

  }

}
