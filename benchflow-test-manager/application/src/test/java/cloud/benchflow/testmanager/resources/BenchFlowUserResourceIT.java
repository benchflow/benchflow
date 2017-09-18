package cloud.benchflow.testmanager.resources;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.DockerComposeIT;
import cloud.benchflow.testmanager.api.response.GetUsersResponse;
import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.exceptions.UserIDAlreadyExistsException;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Jesper Findahl (jesper.findahl@gmail.com) created on 2017-09-18
 */
public class BenchFlowUserResourceIT extends DockerComposeIT {

  @Rule
  public final DropwizardAppRule<BenchFlowTestManagerConfiguration> RULE =
      new DropwizardAppRule<>(BenchFlowTestManagerApplication.class, "../configuration.yml",
          ConfigOverride.config("mongoDB.hostname", MONGO_CONTAINER.getIp()),
          ConfigOverride.config("mongoDB.port", String.valueOf(MONGO_CONTAINER.getExternalPort())),
          ConfigOverride.config("minio.address",
              "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort()),
          ConfigOverride.config("minio.accessKey", MINIO_ACCESS_KEY),
          ConfigOverride.config("minio.secretKey", MINIO_SECRET_KEY),
          ConfigOverride.config("benchFlowExperimentManager.address", "localhost"));

  private UserDAO userDAO;

  @Before
  public void setUp() throws Exception {

    userDAO = BenchFlowTestManagerApplication.getUserDAO();

  }

  @Test
  public void getUsersTest() throws Exception {

    // ############### save test users in DB ##########################
    List<String> userNames = Arrays.asList("userA", "userB", "userC");

    userNames.forEach(username -> {
      try {
        userDAO.addUser(username);
      } catch (UserIDAlreadyExistsException e) {
        e.printStackTrace();
      }
    });

    // ############### query API ######################################

    JerseyClientConfiguration configuration = new JerseyClientConfiguration();
    configuration.setTimeout(Duration.milliseconds(1000));

    Client client =
        new JerseyClientBuilder(RULE.getEnvironment()).using(configuration).build("test client");

    String target = "http://localhost:" + RULE.getLocalPort();

    Response response = client.target(target).path(BenchFlowUserResource.USERS_API).request().get();

    // ############### assert all users retrieved #####################

    List<String> retreivedUserNames = response.readEntity(GetUsersResponse.class).getUsers();

    Assert.assertEquals(userNames.size(), retreivedUserNames.size());

    userNames.forEach(un -> Assert.assertTrue(retreivedUserNames.contains(un)));

  }

}
