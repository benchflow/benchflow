package cloud.benchflow.testmanager.services.internal.dao;

import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.BenchFlowTestNumber;
import cloud.benchflow.testmanager.models.User;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 19.12.16.
 */
public class BenchFlowTestModelDAO extends DAO {

    private static Logger logger = LoggerFactory.getLogger(BenchFlowTestModelDAO.class.getSimpleName());

    public BenchFlowTestModelDAO(MongoClient mongoClient) {
        super(mongoClient);
    }

    /**
     * @param testName
     */
    public synchronized String addTestModel(String testName, User user) {

        logger.info("addTestModel: " + testName);

        long testNumber = generateTestNumber(testName, user);

        BenchFlowTestModel model = new BenchFlowTestModel(user, testName,
                                                              testNumber);
        datastore.save(model);

        user.addTestModel(model);

        datastore.save(user);

        return model.getId();
    }

    /**
     * @param testName
     * @return
     */
    private synchronized long generateTestNumber(String testName, User user) {

        String benchFlowTestIdentifier = BenchFlowTestNumber.generateBenchFlowTestIdentifier(user.getUsername(),
                                                                                                   testName);

        Query<BenchFlowTestNumber> query = datastore
                .createQuery(BenchFlowTestNumber.class)
                .field(BenchFlowTestNumber.ID_FIELD_NAME)
                .equal(benchFlowTestIdentifier);

        UpdateOperations<BenchFlowTestNumber> update = datastore.createUpdateOperations(
                BenchFlowTestNumber.class).inc(BenchFlowTestNumber.COUNTER_FIELD_NAME);

        BenchFlowTestNumber counter = datastore.findAndModify(query, update);

        if (counter == null) {
            counter = new BenchFlowTestNumber(user.getUsername(), testName);
            datastore.save(counter);
        }

        return counter.getCounter();

    }

    /**
     * @param testID
     */
    public synchronized void removeTestModel(String testID) {

        logger.info("removeTestModel: " + testID);

        try {

            BenchFlowTestModel testModel = getTestModel(testID);

            testModel.getExperimentModels().forEach(datastore::delete);

            User user = testModel.getUser();

            user.removeTestModel(testModel);

            datastore.delete(testModel);
            datastore.save(user);

        } catch (BenchFlowTestIDDoesNotExistException e) {
            logger.info("tried to remove non-existent benchflow test");
        }

    }

    /**
     * @param testID
     * @return
     */
    public synchronized BenchFlowTestModel getTestModel(String testID) throws BenchFlowTestIDDoesNotExistException {

        // TODO - this should not be a public method - if an operation is needed we should add a method for it

        logger.info("getTestModel: " + testID);

        final Query<BenchFlowTestModel> testModelQuery = datastore
                .createQuery(BenchFlowTestModel.class)
                .field(BenchFlowTestModel.ID_FIELD_NAME)
                .equal(testID);

        BenchFlowTestModel benchFlowTestModel = testModelQuery.get();

        if (benchFlowTestModel == null)
            throw new BenchFlowTestIDDoesNotExistException();

        return benchFlowTestModel;

    }

    public synchronized boolean testModelExists(String testID) {

        logger.info("testModelExists: " + testID);

        final Query<BenchFlowTestModel> testModelQuery = datastore
                .createQuery(BenchFlowTestModel.class)
                .field(BenchFlowTestModel.ID_FIELD_NAME)
                .equal(testID);

        BenchFlowTestModel benchFlowTestModel = testModelQuery.get();

        return benchFlowTestModel != null;
    }

    /**
     * @return
     */
    public synchronized List<String> getTestModels() {

        logger.info("getTestModels");

        final Query<BenchFlowTestModel> testModelQuery = datastore
                .createQuery(BenchFlowTestModel.class);

        return testModelQuery.asList()
                .stream()
                .map(BenchFlowTestModel::getId)
                .collect(Collectors.toList());
    }

    /**
     * @param testID
     * @param state
     */
    public synchronized BenchFlowTestModel.BenchFlowTestState setTestState(String testID, BenchFlowTestModel.BenchFlowTestState state) throws BenchFlowTestIDDoesNotExistException {

        logger.info("setTestState: " + testID + " : " + state.name());

        final BenchFlowTestModel benchFlowTestModel = getTestModel(testID);

        benchFlowTestModel.setState(state);

        datastore.save(benchFlowTestModel);

        return getTestModel(testID).getState();

    }

    /**
     * @param testID
     * @return
     */
    public synchronized BenchFlowTestModel.BenchFlowTestState getTestState(String testID) throws BenchFlowTestIDDoesNotExistException {

        logger.info("getTestState: " + testID);

        final BenchFlowTestModel benchFlowTestModel = getTestModel(testID);

        return benchFlowTestModel.getState();

    }

    public synchronized List<Long> getExperimentNumbers(String testID) throws BenchFlowTestIDDoesNotExistException {

        logger.info("getExperimentNumbers: " + testID);

        BenchFlowTestModel benchFlowTestModel = getTestModel(testID);

        return benchFlowTestModel.getExperimentNumbers();

    }

}
