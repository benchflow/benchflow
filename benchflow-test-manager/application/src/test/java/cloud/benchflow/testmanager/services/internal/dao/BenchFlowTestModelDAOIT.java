package cloud.benchflow.testmanager.services.internal.dao;

import static cloud.benchflow.testmanager.helpers.constants.TestConstants.LOAD_TEST_NAME;
import static cloud.benchflow.testmanager.models.BenchFlowTestModel.BenchFlowTestState.TERMINATED;
import static org.junit.Assert.assertEquals;

import cloud.benchflow.testmanager.DockerComposeIT;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.User;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 14.02.17.
 */
public class BenchFlowTestModelDAOIT extends DockerComposeIT {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private BenchFlowTestModelDAO testModelDAO;
  private UserDAO userDAO;
  private User testUser;

  @Before
  public void setUp() throws Exception {

    testModelDAO = new BenchFlowTestModelDAO(mongoClient);
    userDAO = new UserDAO(mongoClient, testModelDAO);

    testUser = userDAO.addUser(TestConstants.TEST_USER_NAME);
  }

  @After
  public void tearDown() throws Exception {

    userDAO.removeUser(testUser.getUsername());
  }

  @Test
  public void addGetRemoveBenchFlowTestModel() throws Exception {

    String testID = testModelDAO.addTestModel(LOAD_TEST_NAME, testUser);

    BenchFlowTestModel savedModel = testModelDAO.getTestModel(testID);

    Assert.assertNotNull(savedModel);

    assertEquals(testID, savedModel.getId());

    testModelDAO.removeTestModel(testID);
  }

  @Test
  public void getBenchFlowTestModels() throws Exception {

    // Test IDs
    String testName = "benchFlowTest";

    int initialSize = testModelDAO.getTestModels().size();

    String firstID = testModelDAO.addTestModel(testName, testUser);
    String secondID = testModelDAO.addTestModel(testName, testUser);

    List<String> modelIDs = testModelDAO.getTestModels();

    Assert.assertNotNull(modelIDs);

    assertEquals(initialSize + 2, modelIDs.size());

    String thirdID = testModelDAO.addTestModel(testName, testUser);

    modelIDs = testModelDAO.getTestModels();

    assertEquals(initialSize + 3, modelIDs.size());

    testModelDAO.removeTestModel(firstID);
    testModelDAO.removeTestModel(secondID);

    modelIDs = testModelDAO.getTestModels();

    assertEquals(initialSize + 1, modelIDs.size());

    testModelDAO.removeTestModel(thirdID);

    modelIDs = testModelDAO.getTestModels();

    assertEquals(initialSize + 0, modelIDs.size());
  }

  @Test
  public void conflictingTestModelNames() throws Exception {

    String testIDFirst = testModelDAO.addTestModel(LOAD_TEST_NAME, testUser);

    BenchFlowTestModel model = testModelDAO.getTestModel(testIDFirst);

    Assert.assertNotNull(model);

    String testIDSecond = testModelDAO.addTestModel(LOAD_TEST_NAME, testUser);

    Assert.assertNotEquals(testIDFirst, testIDSecond);

    model = testModelDAO.getTestModel(testIDSecond);

    Assert.assertNotNull(model);

    testModelDAO.removeTestModel(testIDFirst);
    testModelDAO.removeTestModel(testIDSecond);
  }

  @Test
  public void changeBenchFlowTestState() throws Exception {

    String testID = testModelDAO.addTestModel(LOAD_TEST_NAME, testUser);

    BenchFlowTestModel.BenchFlowTestState state = testModelDAO.getTestState(testID);

    assertEquals(BenchFlowTestModel.BenchFlowTestState.START, state);

    testModelDAO.setTestState(testID, TERMINATED);

    state = testModelDAO.getTestState(testID);

    assertEquals(TERMINATED, state);

    testModelDAO.removeTestModel(testID);
  }

  @Test
  public void changeBenchFlowTestStateInvalidID() throws Exception {

    exception.expect(BenchFlowTestIDDoesNotExistException.class);

    testModelDAO.setTestState("not_valid", BenchFlowTestModel.BenchFlowTestState.RUNNING);
  }

  @Test
  public void testHashedID() throws Exception {

    testModelDAO.addTestModel(LOAD_TEST_NAME, testUser);

    DBCollection collection = testModelDAO.getDatastore().getCollection(BenchFlowTestModel.class);

    collection.getIndexInfo().forEach(dbObject -> {
      BasicDBObject index = (BasicDBObject) dbObject;
      if (!index.getString("name").equals("_id_")) {
        assertEquals("hashed",
            ((DBObject) index.get("key")).get(BenchFlowTestModel.HASHED_ID_FIELD_NAME));
      }
    });
  }
}
