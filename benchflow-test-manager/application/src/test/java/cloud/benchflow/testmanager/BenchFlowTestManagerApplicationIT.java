package cloud.benchflow.testmanager;

import cloud.benchflow.dsl.BenchFlowTestAPI;
import cloud.benchflow.dsl.ExplorationSpaceAPI;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.dsl.definition.types.time.Time;
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpace;
import cloud.benchflow.dsl.explorationspace.JavaCompatExplorationSpaceConverter.JavaCompatExplorationSpaceDimensions;
import cloud.benchflow.testmanager.api.request.ChangeBenchFlowTestStateRequest;
import cloud.benchflow.testmanager.api.response.ChangeBenchFlowTestStateResponse;
import cloud.benchflow.testmanager.api.response.ExplorationSpacePointResponse;
import cloud.benchflow.testmanager.api.response.RunBenchFlowTestResponse;
import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.web.InvalidBenchFlowTestIDWebException;
import cloud.benchflow.testmanager.exceptions.web.InvalidTestBundleWebException;
import cloud.benchflow.testmanager.helpers.constants.TestBundle;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.helpers.constants.TestFiles;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.resources.BenchFlowTestResource;
import cloud.benchflow.testmanager.resources.ExplorationPointResource;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import com.mongodb.MongoClient;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import java.io.File;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 18.02.17.
 */
public class BenchFlowTestManagerApplicationIT extends DockerComposeIT {

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

  // needs to be subfolder of current folder for Wercker
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));

  @Test
  public void runBenchFlowTest() throws Exception {

    // needed for multipart client
    // https://github.com/dropwizard/dropwizard/issues/1013
    JerseyClientConfiguration configuration = new JerseyClientConfiguration();
    configuration.setChunkedEncodingEnabled(false);
    // needed because parsing testYaml takes more than default time
    configuration.setTimeout(Duration.milliseconds(5000));

    Client client =
        new JerseyClientBuilder(RULE.getEnvironment()).using(configuration).build("test client");

    String testName = TestConstants.VALID_TEST_NAME;
    User user = BenchFlowConstants.BENCHFLOW_USER;

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getLoadTestBundleFile(temporaryFolder), MediaType.APPLICATION_OCTET_STREAM_TYPE);

    MultiPart multiPart = new MultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
    multiPart.bodyPart(fileDataBodyPart);

    Response response = client.target(String.format("http://localhost:%d/", RULE.getLocalPort()))
        .path(BenchFlowConstants.getPathFromUsername(user.getUsername()))
        .path(BenchFlowConstants.TESTS_PATH).path(BenchFlowTestResource.RUN_PATH)
        .register(MultiPartFeature.class).request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(multiPart, multiPart.getMediaType()));

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    RunBenchFlowTestResponse testResponse = response.readEntity(RunBenchFlowTestResponse.class);

    Assert.assertNotNull(testResponse);
    Assert.assertTrue(testResponse.getTestID().contains(testName));
    Assert.assertTrue(testResponse.getStatus().contains(BenchFlowTestResource.STATUS_PATH));
  }

  @Test
  public void runMissingTestDefinitionTest() throws Exception {

    // needed for multipart client
    // https://github.com/dropwizard/dropwizard/issues/1013
    JerseyClientConfiguration configuration = new JerseyClientConfiguration();
    configuration.setChunkedEncodingEnabled(false);
    // needed because parsing testYaml takes more than default time
    configuration.setTimeout(Duration.milliseconds(5000));

    Client client =
        new JerseyClientBuilder(RULE.getEnvironment()).using(configuration).build("test client");

    User user = BenchFlowConstants.BENCHFLOW_USER;

    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("benchFlowTestBundle",
        TestBundle.getMissingTestDefinitionTestBundleFile(temporaryFolder),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);

    MultiPart multiPart = new MultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
    multiPart.bodyPart(fileDataBodyPart);

    Response response = client.target(String.format("http://localhost:%d/", RULE.getLocalPort()))
        .path(BenchFlowConstants.getPathFromUsername(user.getUsername()))
        .path(BenchFlowConstants.TESTS_PATH).path(BenchFlowTestResource.RUN_PATH)
        .register(MultiPartFeature.class).request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(multiPart, multiPart.getMediaType()));

    Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

    Assert.assertEquals(InvalidTestBundleWebException.message, response.readEntity(String.class));

  }

  @Test
  public void changeTestStateValid() throws Exception {

    BenchFlowTestModelDAO testModelDAO =
        new BenchFlowTestModelDAO(RULE.getConfiguration().getMongoDBFactory().build());

    String testID =
        testModelDAO.addTestModel(TestConstants.LOAD_TEST_NAME, TestConstants.TEST_USER);

    Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

    BenchFlowTestModel.BenchFlowTestState state = BenchFlowTestModel.BenchFlowTestState.TERMINATED;

    ChangeBenchFlowTestStateRequest stateRequest = new ChangeBenchFlowTestStateRequest(state);

    String target = "http://localhost:" + RULE.getLocalPort();

    Response response = client.target(target).path(BenchFlowConstants.getPathFromTestID(testID))
        .path(BenchFlowTestResource.STATE_PATH).request(MediaType.APPLICATION_JSON)
        .put(Entity.entity(stateRequest, MediaType.APPLICATION_JSON));

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ChangeBenchFlowTestStateResponse benchFlowTestStateResponse =
        response.readEntity(ChangeBenchFlowTestStateResponse.class);

    Assert.assertEquals(state, benchFlowTestStateResponse.getState());

  }

  @Test
  public void changeTestStateInvalid() throws Exception {

    Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

    BenchFlowTestModel.BenchFlowTestState state = BenchFlowTestModel.BenchFlowTestState.TERMINATED;
    String testID = TestConstants.LOAD_TEST_ID;

    ChangeBenchFlowTestStateRequest stateRequest = new ChangeBenchFlowTestStateRequest(state);

    String target = "http://localhost:" + RULE.getLocalPort();

    Response response = client.target(target).path(BenchFlowConstants.getPathFromTestID(testID))
        .path(BenchFlowTestResource.STATE_PATH).request(MediaType.APPLICATION_JSON)
        .put(Entity.entity(stateRequest, MediaType.APPLICATION_JSON));

    Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    Assert.assertEquals(InvalidBenchFlowTestIDWebException.message,
        response.readEntity(String.class));

  }

  @Test
  public void getTestStatus() throws Exception {

    // setup the data in the DB

    MongoClient mongoClient = RULE.getConfiguration().getMongoDBFactory().build();

    BenchFlowTestModelDAO testModelDAO = new BenchFlowTestModelDAO(mongoClient);

    BenchFlowExperimentModelDAO experimentModelDAO =
        new BenchFlowExperimentModelDAO(mongoClient, testModelDAO);

    ExplorationModelDAO explorationModelDAO = new ExplorationModelDAO(mongoClient, testModelDAO);

    // get the exploration space and save it in the database

    String testID = testModelDAO.addTestModel(
        TestConstants.TEST_EXPLORATION_ONE_AT_A_TIME_USERS__MEMORY_ENVIRONMENT_NAME,
        TestConstants.TEST_USER);

    String testDefinitionString =
        TestFiles.getTestExplorationOneAtATimeUsersMemoryEnvironmentString();

    JavaCompatExplorationSpace javaCompatExplorationSpace =
        ExplorationSpaceAPI.explorationSpaceFromTestYaml(testDefinitionString);

    JavaCompatExplorationSpaceDimensions javaCompatExplorationSpaceDimensions =
        ExplorationSpaceAPI.explorationSpaceDimensionsFromTestYaml(testDefinitionString);

    explorationModelDAO.setExplorationSpace(testID, javaCompatExplorationSpace);
    explorationModelDAO.setExplorationSpaceDimensions(testID, javaCompatExplorationSpaceDimensions);

    String experimentID = experimentModelDAO.addExperiment(testID);

    experimentModelDAO.setExplorationSpaceIndex(experimentID, 0);

    BenchFlowTest test = BenchFlowTestAPI.testFromYaml(testDefinitionString);

    // add max running time to model
    Time maxTime = test.configuration().terminationCriteria().test().maxTime();
    testModelDAO.setMaxRunTime(testID, maxTime);

    // make the request to the API

    Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

    String target = "http://localhost:" + RULE.getLocalPort();

    Response response = client.target(target).path(BenchFlowConstants.getPathFromTestID(testID))
        .path(BenchFlowTestResource.STATUS_PATH).request().get();

    // assert the response
    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    BenchFlowTestModel testModel = response.readEntity(BenchFlowTestModel.class);
    Assert.assertEquals(testID, testModel.getId());

    Assert.assertTrue(testModel.containsExperimentModel(experimentID));

    Assert.assertEquals(16, testModel.getExplorationModel().getExplorationSpace().getSize());
    Assert.assertTrue(testModel.getExplorationModel().getExplorationSpaceDimensions().getMemory()
        .get().get("camunda").contains("500m"));

    // assert max time

    Assert.assertEquals(maxTime, testModel.getMaxRunningTime());

  }

  @Test
  public void getValidExplorationSpacePointUsers() throws Exception {

    BenchFlowTestModelDAO testModelDAO =
        new BenchFlowTestModelDAO(RULE.getConfiguration().getMongoDBFactory().build());

    BenchFlowExperimentModelDAO experimentModelDAO = new BenchFlowExperimentModelDAO(
        RULE.getConfiguration().getMongoDBFactory().build(), testModelDAO);

    ExplorationModelDAO explorationModelDAO =
        new ExplorationModelDAO(RULE.getConfiguration().getMongoDBFactory().build(), testModelDAO);

    String testID =
        testModelDAO.addTestModel(TestConstants.LOAD_TEST_NAME, TestConstants.TEST_USER);

    String experimentID = experimentModelDAO.addExperiment(testID);
    int explorationPointIndex = 0;

    experimentModelDAO.setExplorationSpaceIndex(experimentID, explorationPointIndex);

    // add exploration space

    String testDefinitionString = TestFiles.getTestExplorationOneAtATimeUsersString();

    JavaCompatExplorationSpace javaCompatExplorationSpace =
        ExplorationSpaceAPI.explorationSpaceFromTestYaml(testDefinitionString);

    explorationModelDAO.setExplorationSpace(testID, javaCompatExplorationSpace);

    // make request

    Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

    String target = "http://localhost:" + RULE.getLocalPort();

    Response response = client.target(target).path(BenchFlowConstants.getPathFromTestID(testID))
        .path(ExplorationPointResource.EXPLORATION_POINT_PATH)
        .path(String.valueOf(explorationPointIndex)).request().get();

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ExplorationSpacePointResponse pointResponse =
        response.readEntity(ExplorationSpacePointResponse.class);

    // assert values

    Assert.assertEquals(5, pointResponse.getUsers().intValue());


  }


}
