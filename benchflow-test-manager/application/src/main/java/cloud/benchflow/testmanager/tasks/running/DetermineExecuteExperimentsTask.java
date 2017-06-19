package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.strategy.selection.SelectionStrategy;
import cloud.benchflow.testmanager.strategy.selection.SelectionStrategy.SelectedExperimentBundle;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determines the next experiment to run and request it to be run on the experiment-manager.
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public class DetermineExecuteExperimentsTask implements Runnable {

  private static Logger logger =
      LoggerFactory.getLogger(DetermineExecuteExperimentsTask.class.getSimpleName());

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
      SelectionStrategy selectionStrategy = explorationModelDAO.getSelectionStrategy(testID);

      // generate the Experiment definition
      SelectedExperimentBundle selectedExperimentBundle =
          selectionStrategy.selectNextExperiment(testID);

      // save exploration point index
      experimentModelDAO.setExplorationSpaceIndex(experimentID,
          selectedExperimentBundle.getExplorationSpaceIndex());

      // set experiment as selected
      explorationModelDAO.addExecutedExplorationPoint(testID,
          selectedExperimentBundle.getExplorationSpaceIndex());

      // save experiment definition on minio
      minioService.saveExperimentDefinition(experimentID, IOUtils.toInputStream(
          selectedExperimentBundle.getExperimentYamlString(), StandardCharsets.UTF_8));

      // save deployment descriptor for experiment
      minioService.saveExperimentDeploymentDescriptor(experimentID, IOUtils.toInputStream(
          selectedExperimentBundle.getDeploymentDescriptorYamlString(), StandardCharsets.UTF_8));

      // save models for experiment
      List<String> bpmnFileNames = minioService.getAllTestBPMNModels(testID);
      bpmnFileNames.forEach(
          fileName -> minioService.copyBPMNModelForExperiment(testID, experimentID, fileName));

      // run experiment on Experiment Manager
      experimentManagerService.runBenchFlowExperiment(experimentID);

    } catch (BenchFlowTestIDDoesNotExistException | BenchFlowExperimentIDDoesNotExistException e) {
      // should not happen
      // TODO - handle this
      e.printStackTrace();
    }

    logger.info("complete: " + testID);
  }
}
