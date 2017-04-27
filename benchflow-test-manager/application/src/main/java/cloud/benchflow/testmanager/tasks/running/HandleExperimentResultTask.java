package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.CompleteSelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;
import cloud.benchflow.testmanager.tasks.BenchFlowTestTaskController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-20
 */
public class HandleExperimentResultTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(HandleExperimentResultTask.class.getSimpleName());

    private final String experimentID;
    private final BenchFlowTestTaskController testTaskController;
    private final BenchFlowTestModelDAO testModelDAO;
    private final ExplorationModelDAO explorationModelDAO;

    public HandleExperimentResultTask(String experimentID) {
        this.experimentID = experimentID;
        this.testTaskController = BenchFlowTestManagerApplication.getTestTaskController();
        this.testModelDAO = BenchFlowTestManagerApplication.getTestModelDAO();
        this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    }

    @Override
    public void run() {

        logger.info("Handling experiment result: " + experimentID);

        String testID = BenchFlowConstants.getTestIDFromExperimentID(experimentID);

        // TODO - get the experiment result

        try {

            ExperimentSelectionStrategy selectionStrategy = explorationModelDAO.getExperimentSelectionStrategy(testID);

            if (selectionStrategy instanceof CompleteSelectionStrategy) {

                // TODO - decide next step (run another experiment or terminate)

                // get exploration space
                // TODO - generalize this to complete search space
                List<Integer> explorationSpace = explorationModelDAO.getWorkloadUserSpace(testID);
                // check which experiments have been executed
                List<Long> executedExperimentNumbers = testModelDAO.getExperimentNumbers(testID);

                if (explorationSpace.size() == executedExperimentNumbers.size()) {

                    testModelDAO.setTestState(testID, BenchFlowTestModel.BenchFlowTestState.TERMINATED);

                } else {
                    testTaskController.runDetermineExecuteExperimentsTask(testID);
                }

            } else {
                // TODO
            }



        } catch (BenchFlowTestIDDoesNotExistException e) {
            e.printStackTrace();
        }


    }
}
