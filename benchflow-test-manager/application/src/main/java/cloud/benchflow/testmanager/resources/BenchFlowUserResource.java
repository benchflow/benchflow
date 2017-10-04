package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.api.response.GetUsersResponse;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import io.swagger.annotations.Api;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 18.12.16.
 */

@Path("/v1/users")
@Api(value = "benchflow-user")
public class BenchFlowUserResource {

  public static String USERS_API = "/v1/users/";

  private Logger logger = LoggerFactory.getLogger(BenchFlowUserResource.class.getSimpleName());

  @Path("/")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public GetUsersResponse getUsers() {

    logger.info("request received: GET " + USERS_API);

    UserDAO userDAO = BenchFlowTestManagerApplication.getUserDAO();

    List usersRaw = userDAO.getUsers();

    List<String> users = new ArrayList<>();

    for (Object o : usersRaw) {
      users.add(o.toString());
    }

    GetUsersResponse response = new GetUsersResponse();

    response.setUsers(users);

    return response;

  }

}
