package cloud.benchflow.testmanager.services.internal.dao;

import static cloud.benchflow.testmanager.helpers.constants.TestConstants.LOAD_TEST_NAME;
import static cloud.benchflow.testmanager.helpers.constants.TestConstants.TEST_USER_NAME;
import static org.junit.Assert.assertEquals;

import cloud.benchflow.testmanager.DockerComposeIT;
import cloud.benchflow.testmanager.exceptions.UserIDAlreadyExistsException;
import cloud.benchflow.testmanager.models.User;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 22.02.17.
 */
public class UserDAOIT extends DockerComposeIT {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private BenchFlowTestModelDAO testModelDAO;
  private UserDAO userDAO;

  @Before
  public void setUp() throws Exception {

    testModelDAO = new BenchFlowTestModelDAO(mongoClient);

    userDAO = new UserDAO(mongoClient, testModelDAO);
  }

  @After
  public void tearDown() throws Exception {

    userDAO.removeUser(TEST_USER_NAME);
  }

  @Test
  public void addRemoveUser() throws Exception {

    User user = userDAO.addUser(TEST_USER_NAME);

    Assert.assertNotNull(user);

    User savedUser = userDAO.getUser(user.getUsername());

    Assert.assertEquals(TEST_USER_NAME, savedUser.getUsername());

    userDAO.removeUser(TEST_USER_NAME);

    user = userDAO.getUser(TEST_USER_NAME);

    Assert.assertNull(user);
  }

  @Test
  public void addSameUserTwice() throws Exception {

    userDAO.addUser(TEST_USER_NAME);

    exception.expect(UserIDAlreadyExistsException.class);

    userDAO.addUser(TEST_USER_NAME);
  }

  @Test
  public void removeUserWithBenchFlowTests() throws Exception {

    User user = userDAO.addUser(TEST_USER_NAME);

    String testModel1ID = testModelDAO.addTestModel(LOAD_TEST_NAME, user);

    user = userDAO.getUser(user.getUsername());

    Assert.assertNotNull(user);

    Assert.assertEquals(1, user.getTestModels().size());

    String testModel2ID = testModelDAO.addTestModel(LOAD_TEST_NAME, user);

    Assert.assertEquals(2, user.getTestModels().size());

    userDAO.removeUser(user.getUsername());

    user = userDAO.getUser(user.getUsername());

    Assert.assertNull(user);

    Assert.assertEquals(false, testModelDAO.testModelExists(testModel1ID));

    Assert.assertEquals(false, testModelDAO.testModelExists(testModel2ID));
  }

  @Test
  public void testHashedID() throws Exception {

    userDAO.addUser(TEST_USER_NAME);

    DBCollection collection = userDAO.getDatastore().getCollection(User.class);

    collection.getIndexInfo().forEach(dbObject -> {
      BasicDBObject index = (BasicDBObject) dbObject;
      if (!index.getString("name").equals("_id_")) {
        assertEquals("hashed", ((DBObject) index.get("key")).get(User.HASHED_ID_FIELD_NAME));
      }
    });
  }
}
