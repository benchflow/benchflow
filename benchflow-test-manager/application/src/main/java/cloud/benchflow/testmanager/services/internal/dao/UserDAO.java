package cloud.benchflow.testmanager.services.internal.dao;

import cloud.benchflow.testmanager.exceptions.UserIDAlreadyExistsException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.BenchFlowTestNumber;
import cloud.benchflow.testmanager.models.User;
import com.mongodb.MongoClient;
import java.util.List;
import java.util.stream.Collectors;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 22.02.17.
 */
public class UserDAO extends DAO {

  private static Logger logger = LoggerFactory.getLogger(UserDAO.class.getSimpleName());

  private BenchFlowTestModelDAO testModelDAO;

  public UserDAO(MongoClient mongoClient, BenchFlowTestModelDAO benchFlowTestModelDAO) {
    super(mongoClient);
    this.testModelDAO = benchFlowTestModelDAO;
  }

  public synchronized User addUser(String username) throws UserIDAlreadyExistsException {

    logger.info("addUser: " + username);

    User user = new User(username);

    if (datastore.get(user) != null) {
      throw new UserIDAlreadyExistsException();
    }

    datastore.save(user);

    return user;
  }

  public synchronized void removeUser(String username) {

    logger.info("removeUser: " + username);

    User user = getUser(username);

    if (user != null) {

      // first remove the reference to the test models from the user and save to DB
      List<String> testModelIDs =
          user.getTestModels().stream().map(BenchFlowTestModel::getId).collect(Collectors.toList());

      user.removeAllTestModels();

      datastore.save(user);

      // remove the test models saved in the DB
      testModelIDs.forEach(testModelID -> testModelDAO.removeTestModel(testModelID));

      // remove the test number counter
      // TODO - change this to remove all counters with IDs that starts with the username
      testModelIDs.stream()
          .map(testModelID -> testModelID.substring(0, testModelID.lastIndexOf(".")))
          .map(id -> datastore.createQuery(BenchFlowTestNumber.class)
              .field(BenchFlowTestNumber.ID_FIELD_NAME).equal(id))
          .forEach(datastore::delete);

      // remove the user from the DB
      datastore.delete(user);
    }
  }

  public synchronized User getUser(String username) {

    logger.info("getUser: " + username);

    final Query<User> testModelQuery =
        datastore.createQuery(User.class).field(User.ID_FIELD_NAME).equal(username);

    return testModelQuery.get();
  }

  public synchronized boolean userExists(User user) {

    return datastore.get(user) != null;
  }

  public synchronized boolean userExists(String username) {

    return getUser(username) != null;
  }
}
