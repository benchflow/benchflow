package cloud.benchflow.experimentmanager.services.internal.dao;

import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.RunningState;
import cloud.benchflow.experimentmanager.models.TrialModel;
import cloud.benchflow.faban.client.responses.RunStatus;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.BenchFlowExperimentState;
import static cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.ID_FIELD_NAME;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 06.03.17.
 */
public class BenchFlowExperimentModelDAO {

    private static Logger logger = LoggerFactory.getLogger(BenchFlowExperimentModelDAO.class.getSimpleName());

    private Datastore datastore;

    public BenchFlowExperimentModelDAO(MongoClient mongoClient) {

        final Morphia morphia = new Morphia();

        // tell Morphia where to find your classes
        // can be called multiple times with different packages or classes
        morphia.map(BenchFlowExperimentModel.class);
        morphia.map(TrialModel.class);

        // create the Datastore
        // TODO - set-up mongo DB (http://mongodb.github.io/mongo-java-driver/2.13/getting-started/quick-tour/)
        // TODO - check about resilience and cache
        datastore = morphia.createDatastore(mongoClient, BenchFlowConstants.DB_NAME);
        datastore.ensureIndexes();

    }

    public synchronized void addExperiment(String experimentID) {

        logger.info("addExperiment: " + experimentID);

        BenchFlowExperimentModel experimentModel = new BenchFlowExperimentModel(experimentID);

        datastore.save(experimentModel);

    }

    public synchronized BenchFlowExperimentModel getExperimentModel(String experimentID) {

        logger.info("getExperimentModel: " + experimentID);

        return datastore.createQuery(BenchFlowExperimentModel.class)
                .field(ID_FIELD_NAME)
                .equal(experimentID)
                .get();

    }

    public synchronized BenchFlowExperimentState getExperimentState(String experimentID) throws BenchFlowExperimentIDDoesNotExistException {

        logger.info("getExperimentState: " + experimentID);

        BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

        if (experimentModel == null) {
            throw new BenchFlowExperimentIDDoesNotExistException();
        }

        return experimentModel.getState();

    }

    public synchronized BenchFlowExperimentState setExperimentState(String experimentID, BenchFlowExperimentState state) {

        logger.info("setExperimentRunningState: " + experimentID + " to " + state.name());

        BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

        if (experimentModel == null) {
            return null;
        }

        experimentModel.setState(state);

        datastore.save(experimentModel);

        return experimentModel.getState();

    }

    public synchronized RunningState getRunningState(String experimentID) throws BenchFlowExperimentIDDoesNotExistException {

        logger.info("getExperimentState: " + experimentID);

        BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

        if (experimentModel == null) {
            throw new BenchFlowExperimentIDDoesNotExistException();
        }

        return experimentModel.getRunningState();

    }

    public synchronized BenchFlowExperimentState setRunningState(String experimentID, RunningState state) {

        logger.info("setRunningState: " + experimentID + " state: " + state.name());

        BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

        if (experimentModel == null) {
            return null;
        }

        experimentModel.setRunningState(state);

        datastore.save(experimentModel);

        return experimentModel.getState();

    }

    public synchronized BenchFlowExperimentModel.TerminatedState getTerminatedState(String experimentID) throws BenchFlowExperimentIDDoesNotExistException {

        logger.info("getTerminatedState: " + experimentID);

        BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

        if (experimentModel == null) {
            throw new BenchFlowExperimentIDDoesNotExistException();
        }

        return experimentModel.getTerminatedState();

    }

    public synchronized BenchFlowExperimentModel.TerminatedState setTerminatedState(String experimentID, BenchFlowExperimentModel.TerminatedState state) {

        logger.info("setTerminatedState: " + experimentID + " state: " + state.name());

        BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

        if (experimentModel == null) {
            return null;
        }

        experimentModel.setTerminatedState(state);

        datastore.save(experimentModel);

        return experimentModel.getTerminatedState();

    }

    public synchronized String addTrial(String experimentID, long trialNumber) throws BenchFlowExperimentIDDoesNotExistException {

        String trialID = getTrialID(experimentID, trialNumber);

        logger.info("addTrial: " + trialID);

        BenchFlowExperimentModel experimentModel = getExperimentModel(experimentID);

        if (experimentModel == null) {
            throw new BenchFlowExperimentIDDoesNotExistException();
        }

        // first save the Trial Model and then add it to Experiment Model
        TrialModel trialModel = new TrialModel(trialID);
        datastore.save(trialModel);

        experimentModel.addTrial(trialModel);

        datastore.save(experimentModel);

        return trialID;

    }

    private synchronized TrialModel getTrialModel(String trialID) {

        logger.info("getTrialModel: " + trialID);

        return datastore.createQuery(TrialModel.class)
                .field(TrialModel.ID_FIELD_NAME)
                .equal(trialID)
                .get();

    }

    private synchronized TrialModel getTrialModel(String experimentID, long trialNumber) {

        String trialID = getTrialID(experimentID, trialNumber);

        return getTrialModel(trialID);

    }

    public synchronized void setTrialModelAsStarted(String experimentID, long trialNumber) throws TrialIDDoesNotExistException {

        logger.info("setTrialModelAsStarted: " + experimentID + " trial " + trialNumber);

        setTrialStatus(experimentID, trialNumber, RunStatus.Code.STARTED);

    }

    public synchronized void setFabanTrialID(String experimentID, long trialNumber, String fabanRunID) {

        logger.info("setFabanTrialID: " + experimentID + " trial " + trialNumber + " with " + fabanRunID);

        TrialModel trialModel = getTrialModel(experimentID, trialNumber);

        if (trialModel != null) {
            trialModel.setFabanRunID(fabanRunID);
            datastore.save(trialModel);
        }

    }

    public synchronized void setTrialStatus(String experimentID, long trialNumber, RunStatus.Code statusCode) {

        logger.info("setTrialStatus: " + experimentID + " trial " + trialNumber + " with " + statusCode.name());

        TrialModel trialModel = getTrialModel(experimentID, trialNumber);

        if (trialModel != null) {
            trialModel.setStatus(statusCode);
            datastore.save(trialModel);
        }


    }

    private synchronized String getTrialID(String experimentID, long trialNumber) {
        return experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + trialNumber;
    }
}
