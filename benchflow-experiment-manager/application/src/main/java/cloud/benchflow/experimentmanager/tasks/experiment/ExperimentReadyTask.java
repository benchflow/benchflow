package cloud.benchflow.experimentmanager.tasks.experiment;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowExperiment;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.definition.workload.Workload;
import cloud.benchflow.dsl.demo.DemoConverter;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel.TerminatedState;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.tasks.CancellableTask;
import cloud.benchflow.experimentmanager.tasks.ExperimentTaskController;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-19
 */
public class ExperimentReadyTask extends CancellableTask {

  private static final String TEMP_DIR = "./tmp";
  private static final String BENCHMARK_FILE_ENDING = ".jar";

  private static Logger logger = LoggerFactory.getLogger(ExperimentReadyTask.class.getSimpleName());

  private String experimentID;

  private ExperimentTaskController experimentTaskController;
  private BenchFlowExperimentModelDAO experimentModelDAO;
  private MinioService minioService;
  private FabanClient fabanClient;
  private DriversMakerService driversMakerService;
  private BenchFlowTestManagerService testManagerService;

  public ExperimentReadyTask(String experimentID, ExperimentTaskController experimentTaskController, BenchFlowExperimentModelDAO experimentModelDAO, MinioService minioService, FabanClient fabanClient, DriversMakerService driversMakerService, BenchFlowTestManagerService testManagerService) {
    this.experimentID = experimentID;
    this.experimentTaskController = experimentTaskController;
    this.experimentModelDAO = experimentModelDAO;
    this.minioService = minioService;
    this.fabanClient = fabanClient;
    this.driversMakerService = driversMakerService;
    this.testManagerService = testManagerService;
  }

  @Override
  public void run() {

    // DEPLOY EXPERIMENT TO FABAN

    logger.info("deploying experiment: " + experimentID);

    try {

      // get the BenchFlowExperimentDefinition from minio
      String experimentYamlString = IOUtils.toString(minioService.getExperimentDefinition(experimentID), StandardCharsets.UTF_8);

      BenchFlowExperiment experiment = BenchFlowDSL.experimentFromExperimentYaml(experimentYamlString);

      int nTrials = experiment.configuration().terminationCriteria().get().experiment().number();

      // save experiment model in DB
      experimentModelDAO.addExperiment(experimentID);

      // convert to old version and save to minio, and also a new experimentID to send to DriversMaker
      // generate DriversMaker compatible files on minio
      DriversMakerCompatibleID driversMakerCompatibleID = new DriversMakerCompatibleID(experimentID);

      String driversMakerExperimentID = driversMakerCompatibleID.getDriversMakerExperimentID();
      String experimentName = driversMakerCompatibleID.getExperimentName();
      long experimentNumber = driversMakerCompatibleID.getExperimentNumber();

      saveDriversMakerCompatibleFilesOnMinio(experiment, driversMakerExperimentID, experimentNumber);

      // generate benchmark from DriversMaker (one per experiment)
      // TODO - is driversMakerService responsible to generate the trialIDs, otherwise I think we should just pass the trialID to the driversMakerService

      driversMakerService.generateBenchmark(experimentName, experimentNumber, nTrials);

      // DEPLOY TO FABAN
      // get the generated benchflow-benchmark.jar from minioService and save to disk so that it can be sent
      InputStream fabanBenchmark = minioService.getDriversMakerGeneratedBenchmark(driversMakerExperimentID, experimentNumber);

      // TODO - should this be a method (part of Faban Client?)
      String fabanExperimentId = experimentID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);

      // store on disk because there are issues sending InputStream directly
      java.nio.file.Path benchmarkPath =
          Paths.get(TEMP_DIR)
              .resolve(experimentID)
              .resolve(fabanExperimentId + BENCHMARK_FILE_ENDING);

      FileUtils.copyInputStreamToFile(fabanBenchmark, benchmarkPath.toFile());

      // deploy experiment to Faban
      fabanClient.deploy(benchmarkPath.toFile());
      logger.info("deployed benchmark to Faban: " + fabanExperimentId); // TODO - move this into fabanClient

      // remove file that was sent to fabanClient
      FileUtils.forceDelete(benchmarkPath.toFile());

      // schedule experiment execution
      if (!isCanceled.booleanValue()) {
        experimentTaskController.runExperiment(experimentID);
      }


    } catch (IOException e) {

      logger.error("could not read experiment definition for " + experimentID + " : " + e.getMessage());
      experimentModelDAO.setTerminatedState(experimentID, TerminatedState.ERROR);
      testManagerService.setExperimentTerminatedState(experimentID, TerminatedState.ERROR);
      e.printStackTrace();

    } catch (JarFileNotFoundException e) {

      logger.error("could not find jar for " + experimentID + " : " + e.getMessage());
      experimentModelDAO.setTerminatedState(experimentID, TerminatedState.ERROR);
      testManagerService.setExperimentTerminatedState(experimentID, TerminatedState.ERROR);
      e.printStackTrace();

    } catch (BenchFlowDeserializationException e) {
      // should already have been checked in previous step
      // TODO - handle me
      e.printStackTrace();
    }


  }

  private void saveDriversMakerCompatibleFilesOnMinio(BenchFlowExperiment experiment, String driversMakerExperimentID, long experimentNumber) {

    // convert to previous version
    String oldExperimentDefinitionYaml = DemoConverter.convertExperimentToPreviousYamlString(experiment);
    InputStream definitionInputStream = IOUtils.toInputStream(oldExperimentDefinitionYaml, StandardCharsets.UTF_8);

    minioService.copyExperimentDefintionForDriversMaker(driversMakerExperimentID, experimentNumber, definitionInputStream);
    minioService.copyDeploymentDescriptorForDriversMaker(experimentID, driversMakerExperimentID, experimentNumber);

    // convert to Java compatible type
    Collection<Workload> workloadCollection = JavaConverters.asJavaCollectionConverter(experiment.workload().values()).asJavaCollection();

    for (Workload workload : workloadCollection) {

      for (String operationName : JavaConverters.asJavaCollectionConverter(workload.operations()).asJavaCollection()) {
        minioService.copyExperimentBPMNModelForDriversMaker(experimentID, driversMakerExperimentID, operationName);
      }
    }
  }
}
