package cloud.benchflow.testmanager.services.internal.dao;

import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;
import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-25
 */
public class ExplorationModelDAO {

    private static Logger logger = LoggerFactory.getLogger(ExplorationModelDAO.class.getSimpleName());

    private Datastore datastore;
    private BenchFlowTestModelDAO testModelDAO;

    public ExplorationModelDAO(Datastore datastore, BenchFlowTestModelDAO testModelDAO) {
        this.datastore = datastore;
        this.testModelDAO = testModelDAO;
    }

    public synchronized List<Integer> getWorkloadUserSpace(String testID) throws BenchFlowTestIDDoesNotExistException {

        logger.info("getWorkloadUserSpace: " + testID);

        final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

        return benchFlowTestModel.getExplorationModel().getWorkloadUsersSpace();

    }

    public synchronized void setWorkloadUserSpace(String testID, List<Integer> workloadUserSpace) throws BenchFlowTestIDDoesNotExistException {

        logger.info("setWorkloadUserSpace: " + testID);

        final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

        benchFlowTestModel.getExplorationModel().setWorkloadUsersSpace(workloadUserSpace);

        datastore.save(benchFlowTestModel);

    }

    public synchronized ExperimentSelectionStrategy getExperimentSelectionStrategy(String testID) throws BenchFlowTestIDDoesNotExistException {

        logger.info("getExperimentSelectionStrategy: " + testID);

        final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

        return benchFlowTestModel.getExplorationModel().getExperimentSelectionStrategy();

    }

    public synchronized void setExperimentSelectionStrategy(String testID, ExperimentSelectionStrategy experimentSelectionStrategy) throws BenchFlowTestIDDoesNotExistException {

        logger.info("setExperimentSelectionStrategy: " + testID);

        final BenchFlowTestModel benchFlowTestModel = testModelDAO.getTestModel(testID);

        benchFlowTestModel.getExplorationModel().setExperimentSelectionStrategy(experimentSelectionStrategy);

        datastore.save(benchFlowTestModel);

    }

}
