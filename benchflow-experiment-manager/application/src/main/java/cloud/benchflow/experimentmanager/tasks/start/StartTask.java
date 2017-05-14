package cloud.benchflow.experimentmanager.tasks.start;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowExperiment;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.definition.workload.Workload;
import cloud.benchflow.dsl.demo.DemoConverter;
import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.collection.JavaConverters;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class StartTask implements Callable<Boolean> {

  private static final String TEMP_DIR = "./tmp";
  private static final String BENCHMARK_FILE_ENDING = ".jar";

  private static Logger logger = LoggerFactory.getLogger(StartTask.class.getSimpleName());

  private String experimentID;

  private BenchFlowExperimentModelDAO experimentModelDAO;
  private MinioService minioService;
  private FabanClient fabanClient;
  private DriversMakerService driversMakerService;

  public StartTask(String experimentID) {

    this.experimentID = experimentID;

    this.experimentModelDAO = BenchFlowExperimentManagerApplication.getExperimentModelDAO();
    this.minioService = BenchFlowExperimentManagerApplication.getMinioService();
    this.fabanClient = BenchFlowExperimentManagerApplication.getFabanClient();
    this.driversMakerService = BenchFlowExperimentManagerApplication.getDriversMakerService();
  }

  @Override
  public Boolean call() throws Exception {

    // DEPLOY EXPERIMENT TO FABAN

    logger.info("deploying experiment: " + experimentID);

    try {

      // get the BenchFlowExperimentDefinition from minio
      String experimentYamlString = IOUtils
          .toString(minioService.getExperimentDefinition(experimentID), StandardCharsets.UTF_8);

      BenchFlowExperiment experiment =
          BenchFlowDSL.experimentFromExperimentYaml(experimentYamlString);

      int nTrials = experiment.configuration().terminationCriteria().get().experiment().number();

      // save number of trials
      experimentModelDAO.setNumTrials(experimentID, nTrials);

      // convert to old version and save to minio, and also a new experimentID to send to DriversMaker
      // generate DriversMaker compatible files on minio
      DriversMakerCompatibleID driversMakerCompatibleID =
          new DriversMakerCompatibleID(experimentID);

      String driversMakerExperimentID = driversMakerCompatibleID.getDriversMakerExperimentID();
      String experimentName = driversMakerCompatibleID.getExperimentName();
      long experimentNumber = driversMakerCompatibleID.getExperimentNumber();

      saveDriversMakerCompatibleFilesOnMinio(experiment, driversMakerExperimentID,
          experimentNumber);

      // generate benchmark from DriversMaker (one per experiment)
      // TODO - is driversMakerService responsible to generate the trialIDs, otherwise I think we should just pass the trialID to the driversMakerService

      driversMakerService.generateBenchmark(experimentName, experimentNumber, nTrials);

      // DEPLOY TO FABAN
      // get the generated benchflow-benchmark.jar from minioService and save to disk so that it can be sent
      InputStream fabanBenchmark = minioService
          .getDriversMakerGeneratedBenchmark(driversMakerExperimentID, experimentNumber);

      // TODO - should this be a method (part of Faban Client?)
      String fabanExperimentId =
          experimentID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);

      // store on disk because there are issues sending InputStream directly
      java.nio.file.Path benchmarkPath = Paths.get(TEMP_DIR).resolve(experimentID)
          .resolve(fabanExperimentId + BENCHMARK_FILE_ENDING);

      FileUtils.copyInputStreamToFile(fabanBenchmark, benchmarkPath.toFile());

      // deploy experiment to Faban
      fabanClient.deploy(benchmarkPath.toFile());
      logger.info("deployed benchmark to Faban: " + fabanExperimentId); // TODO - move this into fabanClient

      // remove file that was sent to fabanClient
      FileUtils.forceDelete(benchmarkPath.toFile());

      // deployment successful
      return true;

    } catch (IOException e) {

      logger.error(
          "could not read experiment definition for " + experimentID + " : " + e.getMessage());

      return false;

    } catch (JarFileNotFoundException e) {

      logger.error("could not find jar for " + experimentID + " : " + e.getMessage());

      return false;

    } catch (BenchFlowDeserializationException e) {
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

    // convert to Java compatible type
    Collection<Workload> workloadCollection =
        JavaConverters.asJavaCollectionConverter(experiment.workload().values()).asJavaCollection();

    for (Workload workload : workloadCollection) {

      for (String operationName : JavaConverters.asJavaCollectionConverter(workload.operations())
          .asJavaCollection()) {
        minioService.copyExperimentBPMNModelForDriversMaker(experimentID, driversMakerExperimentID,
            operationName);
      }

    }

    // TODO - think how to handle this is a cleaner way
    // copy necessary mock.bpmn
    minioService.copyExperimentBPMNModelForDriversMaker(experimentID, driversMakerExperimentID,
        "mock.bpmn");
  }
}
