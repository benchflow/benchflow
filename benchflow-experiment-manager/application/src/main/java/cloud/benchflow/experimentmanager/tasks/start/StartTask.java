package cloud.benchflow.experimentmanager.tasks.start;

import cloud.benchflow.dsl.BenchFlowExperimentAPI;
import cloud.benchflow.dsl.definition.BenchFlowExperiment;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.demo.DemoConverter;
import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.exceptions.BenchMarkDeploymentException;
import cloud.benchflow.experimentmanager.exceptions.BenchmarkGenerationException;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.tasks.AbortableCallable;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class StartTask extends AbortableCallable<Boolean> {

  private static Logger logger = LoggerFactory.getLogger(StartTask.class.getSimpleName());

  private String experimentID;

  private BenchFlowExperimentModelDAO experimentModelDAO;
  private MinioService minioService;
  private FabanManagerService fabanManagerService;
  private DriversMakerService driversMakerService;

  public StartTask(String experimentID) {

    this.experimentID = experimentID;

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.minioService = BenchFlowExperimentManagerApplication.getMinioService();
    this.fabanManagerService = BenchFlowExperimentManagerApplication.getFabanManagerService();
    this.driversMakerService = BenchFlowExperimentManagerApplication.getDriversMakerService();
  }

  @VisibleForTesting
  public StartTask(String experimentID, BenchFlowExperimentModelDAO experimentModelDAO,
      MinioService minioService, FabanManagerService fabanManagerService,
      DriversMakerService driversMakerService) {
    this.experimentID = experimentID;
    this.experimentModelDAO = experimentModelDAO;
    this.minioService = minioService;
    this.fabanManagerService = fabanManagerService;
    this.driversMakerService = driversMakerService;
  }

  @Override
  public Boolean call() {

    // DEPLOY EXPERIMENT TO FABAN

    logger.info("deploying experiment: " + experimentID);

    try {

      // get the BenchFlowExperimentDefinition from minio
      String experimentYamlString = IOUtils
          .toString(minioService.getExperimentDefinition(experimentID), StandardCharsets.UTF_8);

      BenchFlowExperiment experiment =
          BenchFlowExperimentAPI.experimentFromExperimentYaml(experimentYamlString);

      int numTrials =
          experiment.configuration().terminationCriteria().experiment().numberOfTrials();

      // save number of trials
      experimentModelDAO.setNumTrials(experimentID, numTrials);

      // Convert to old version and save to minio, and also a new experimentID to
      // send to DriversMaker.
      // Generate DriversMaker compatible files on minio
      DriversMakerCompatibleID driversMakerCompatibleID =
          new DriversMakerCompatibleID(experimentID);

      String driversMakerExperimentID = driversMakerCompatibleID.getDriversMakerExperimentID();
      String experimentName = driversMakerCompatibleID.getExperimentName();
      long experimentNumber = driversMakerCompatibleID.getExperimentNumber();

      saveDriversMakerCompatibleFilesOnMinio(experiment, driversMakerExperimentID,
          experimentNumber);

      // generate benchmark from DriversMaker (one per experiment)
      // TODO - is driversMakerService responsible to generate the trialIDs,
      // otherwise I (JF) think we should just pass the trialID to the driversMakerService

      driversMakerService.generateBenchmark(experimentName, experimentNumber, numTrials);

      fabanManagerService.deployExperimentToFaban(experimentID, driversMakerExperimentID,
          experimentNumber);

      // deployment successful
      return true;

    } catch (IOException e) {

      logger.error(
          "could not read experiment bundle files for " + experimentID + " : " + e.getMessage());

      return false;

    } catch (BenchmarkGenerationException e) {
      logger.info("BenchmarkGenerationException - " + e.getMessage());
      return false;
    } catch (BenchMarkDeploymentException e) {
      logger.info("BenchMarkDeploymentException - " + e.getMessage());
      return false;
    } catch (BenchFlowDeserializationException | BenchFlowExperimentIDDoesNotExistException e) {
      // should already have been checked in previous step
      // TODO - handle me
      e.printStackTrace();
      return false;
    }

  }

  private void saveDriversMakerCompatibleFilesOnMinio(BenchFlowExperiment experiment,
      String driversMakerExperimentID, long experimentNumber) {

    // convert to previous version
    String oldExperimentDefinitionYaml =
        DemoConverter.convertExperimentToPreviousYamlString(experiment);
    InputStream definitionInputStream =
        IOUtils.toInputStream(oldExperimentDefinitionYaml, StandardCharsets.UTF_8);

    minioService.copyExperimentDefintionForDriversMaker(driversMakerExperimentID, experimentNumber,
        definitionInputStream);
    minioService.copyDeploymentDescriptorForDriversMaker(experimentID, driversMakerExperimentID,
        experimentNumber);
    
    // save models for experiment
    List<String> bpmnFileNames = minioService.getAllTestBPMNModels(experimentID);
    bpmnFileNames.forEach(fileName -> minioService
        .copyExperimentBPMNModelForDriversMaker(experimentID, driversMakerExperimentID, fileName));

    // TODO - think how to handle this is a cleaner way
    // copy necessary mock.bpmn
    minioService.copyExperimentBPMNModelForDriversMaker(experimentID, driversMakerExperimentID,
        "mock.bpmn");
  }
}
