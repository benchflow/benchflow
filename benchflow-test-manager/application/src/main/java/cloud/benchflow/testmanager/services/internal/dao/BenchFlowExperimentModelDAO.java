package cloud.benchflow.testmanager.services.internal.dao;

import cloud.benchflow.faban.client.responses.RunStatus;
import cloud.benchflow.testmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import cloud.benchflow.testmanager.models.BenchFlowExperimentModel.BenchFlowExperimentStatus;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 22.02.17.
 */
public class BenchFlowExperimentModelDAO {

    // TODO - this is also stored in the model?? read it directly from the model
    private static final String BENCHFLOW_EXPERIMENT_ID_FIELD_NAME = "id";

    private static Logger logger = LoggerFactory.getLogger(BenchFlowExperimentModelDAO.class.getSimpleName());

    private Datastore datastore;
    private BenchFlowTestModelDAO testModelDAO;

    public BenchFlowExperimentModelDAO(MongoClient mongoClient, BenchFlowTestModelDAO testModelDAO) {

        this.testModelDAO = testModelDAO;

        final Morphia morphia = new Morphia();

        // tell Morphia where to find your classes
        // can be called multiple times with different packages or classes
        morphia.map(BenchFlowExperimentModel.class);

        // create the Datastore
        // TODO - set-up mongo DB (http://mongodb.github.io/mongo-java-driver/2.13/getting-started/quick-tour/)
        // TODO - check about resilience and cache
        datastore = morphia.createDatastore(mongoClient, BenchFlowConstants.DB_NAME);
        datastore.ensureIndexes();

    }

    /**
     * @param testID
     * @return
     * @throws BenchFlowTestIDDoesNotExistException
     */
    public synchronized String addExperiment(String testID) throws BenchFlowTestIDDoesNotExistException {

        logger.info("addExperiment: " + testID);

        final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

        long experimentNumber = benchFlowTestModel.getNextExperimentNumber();

        BenchFlowExperimentModel experimentModel = new BenchFlowExperimentModel(testID,
                                                                                    experimentNumber);

        // first save the PE model and then add it to PT Model
        datastore.save(experimentModel);

        benchFlowTestModel.addExperimentModel(experimentModel);

        datastore.save(benchFlowTestModel);

        return experimentModel.getId();

    }

    /**
     * @param experimentID
     * @return
     * @throws BenchFlowExperimentIDDoesNotExistException
     */
    private synchronized BenchFlowExperimentModel getExperiment(String experimentID) throws BenchFlowExperimentIDDoesNotExistException {

        logger.info("getExperiment: " + experimentID);

        final Query<BenchFlowExperimentModel> testModelQuery = datastore
                .createQuery(BenchFlowExperimentModel.class)
                .field(BENCHFLOW_EXPERIMENT_ID_FIELD_NAME)
                .equal(experimentID);

        BenchFlowExperimentModel experimentModel = testModelQuery.get();

        if (experimentModel == null)
            throw new BenchFlowExperimentIDDoesNotExistException();

        return experimentModel;

    }

    /**
     *
     * @param experimentID
     * @param state
     * @throws BenchFlowExperimentIDDoesNotExistException
     */
    public synchronized void setExperimentState(String experimentID, BenchFlowExperimentState state, BenchFlowExperimentStatus status) throws BenchFlowExperimentIDDoesNotExistException {

        logger.info("setExperimentState: " + experimentID + " : " + state.name());

        final BenchFlowExperimentModel experimentModel;

        experimentModel = getExperiment(experimentID);

        experimentModel.setState(state);
        experimentModel.setStatus(status);

        datastore.save(experimentModel);

    }

    /**
     * @param experimentID
     * @param trialNUmber
     * @param status
     * @throws BenchFlowTestIDDoesNotExistException
     */
    public synchronized void addTrialStatus(String experimentID, long trialNUmber, RunStatus.Code status) throws BenchFlowExperimentIDDoesNotExistException {

        logger.info(
                "addTrialStatus: " + experimentID + MODEL_ID_DELIMITER + trialNUmber + " : " + status.name());

        final BenchFlowExperimentModel experimentModel;

        experimentModel = getExperiment(experimentID);

        experimentModel.setTrialStatus(trialNUmber, status);
        datastore.save(experimentModel);

    }

    /**
     * @param experimentID
     * @param trialNumber
     * @return
     * @throws BenchFlowExperimentIDDoesNotExistException
     */
    public synchronized RunStatus.Code getTrialStatus(String experimentID, long trialNumber) throws BenchFlowExperimentIDDoesNotExistException {

        logger.info("getTrialStatus: " + experimentID + MODEL_ID_DELIMITER + trialNumber);

        final BenchFlowExperimentModel experimentModel = getExperiment(experimentID);

        return experimentModel.getTrialStatus(trialNumber);

    }
}
