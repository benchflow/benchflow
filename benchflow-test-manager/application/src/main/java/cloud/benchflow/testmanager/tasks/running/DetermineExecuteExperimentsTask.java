package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowExperiment;
import cloud.benchflow.dsl.definition.BenchFlowTest;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.models.BenchFlowTestModel;
import cloud.benchflow.testmanager.models.ExplorationModel;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.CompleteSelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.ExperimentSelectionStrategy;
import cloud.benchflow.testmanager.tasks.BenchFlowTestTaskController;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-20
 */
public class DetermineExecuteExperimentsTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(DetermineExecuteExperimentsTask.class.getSimpleName());

    private final String testID;

    // services
    private final MinioService minioService;
    private final BenchFlowExperimentManagerService experimentManagerService;
    private final ExplorationModelDAO explorationModelDAO;
    private final BenchFlowExperimentModelDAO experimentModelDAO;

    public DetermineExecuteExperimentsTask(String testID) {

        this.testID = testID;

        this.minioService = BenchFlowTestManagerApplication.getMinioService();
        this.experimentManagerService = BenchFlowTestManagerApplication.getExperimentManagerService();
        this.experimentModelDAO = BenchFlowTestManagerApplication.getExperimentModelDAO();
        this.explorationModelDAO = BenchFlowTestManagerApplication.getExplorationModelDAO();
    }

    @Override
    public void run() {

        logger.info("running: " + testID);

        try {

            // add new experiment model
            String experimentID = experimentModelDAO.addExperiment(testID);

            // get the selection strategy
            ExperimentSelectionStrategy selectionStrategy = explorationModelDAO.getExperimentSelectionStrategy(testID);

            // generate the Experiment definition
            String experimentYaml = selectionStrategy.selectNextExperiment(testID);
            InputStream experimentYamlInputStream = IOUtils.toInputStream(experimentYaml, StandardCharsets.UTF_8);

            // save PE defintion to minio
            minioService.saveExperimentDefinition(experimentID,
                    experimentYamlInputStream);

            // save deployment descriptor for experiment
            minioService.copyDeploymentDescriptorForExperiment(testID, experimentID);

            // save models for experiment
            List<String> bpmnFileNames = minioService.getAllTestBPMNModels(testID);
            bpmnFileNames.forEach(fileName -> minioService.copyBPMNModelForExperiment(testID, experimentID, fileName));

            // run PE on PEManager
            experimentManagerService.runBenchFlowExperiment(experimentID);

        } catch (BenchFlowTestIDDoesNotExistException e) {
            e.printStackTrace();
        }

        logger.info("complete: " + testID);

    }
}
