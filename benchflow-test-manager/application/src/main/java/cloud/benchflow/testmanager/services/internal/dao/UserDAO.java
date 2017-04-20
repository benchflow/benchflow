package cloud.benchflow.testmanager.services.internal.dao;

import cloud.benchflow.testmanager.exceptions.UserIDAlreadyExistsException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.BenchFlowTestNumber;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 22.02.17.
 */
public class UserDAO {

    private static Logger logger = LoggerFactory.getLogger(UserDAO.class.getSimpleName());

    private Datastore datastore;
    private BenchFlowTestModelDAO testModelDAO;

    public UserDAO(MongoClient mongoClient, BenchFlowTestModelDAO benchFlowTestModelDAO) {

        this.testModelDAO = benchFlowTestModelDAO;

        final Morphia morphia = new Morphia();

        // tell Morphia where to find your classes
        // can be called multiple times with different packages or classes
        morphia.map(User.class);

        // create the Datastore
        // TODO - set-up mongo DB (http://mongodb.github.io/mongo-java-driver/2.13/getting-started/quick-tour/)
        // TODO - check about resilience and cache
        datastore = morphia.createDatastore(mongoClient, BenchFlowConstants.DB_NAME);
        datastore.ensureIndexes();

    }

    public synchronized User addUser(String username) throws UserIDAlreadyExistsException {

        logger.info("addUser: " + username);

        User user = new User(username);

        if (datastore.get(user) != null)
            throw new UserIDAlreadyExistsException();

        datastore.save(user);

        return user;

    }

    /**
     * @param username
     */
    public synchronized void removeUser(String username) {

        logger.info("removeUser: " + username);

        User user = getUser(username);

        if (user != null) {

            // first remove the reference to the test models from the user and save to DB
            List<String> testModelIDs = user.getTestModels().stream().map(BenchFlowTestModel::getId).collect(
                    Collectors.toList());

            user.removeAllTestModels();

            datastore.save(user);

            // remove the test models saved in the DB
            testModelIDs.forEach(testModelID -> testModelDAO.removeTestModel(testModelID));

            // remove the test number counter
            // TODO - change this to remove all counters with IDs that starts with the username
            testModelIDs.stream()
                    .map(testModelID -> testModelID.substring(0, testModelID.lastIndexOf(".")))
                    .map(id -> datastore.createQuery(BenchFlowTestNumber.class)
                            .field(BenchFlowTestNumber.ID_FIELD_NAME)
                            .equal(id))
                    .forEach(query -> datastore.delete(query));

            // remove the user from the DB
            datastore.delete(user);
        }

    }

    public synchronized User getUser(String username) {

        logger.info("getUser: " + username);

        final Query<User> testModelQuery = datastore
                .createQuery(User.class)
                .field(User.ID_FIELD_NAME)
                .equal(username);

        return testModelQuery.get();

    }

    public synchronized boolean userExists(User user) {

        return datastore.get(user) != null;

    }

    public synchronized boolean userExists(String username) {

        return getUser(username) != null;

    }
}
