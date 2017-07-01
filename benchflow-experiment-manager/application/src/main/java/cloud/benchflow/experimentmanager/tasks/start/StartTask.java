package cloud.benchflow.experimentmanager.tasks.start;

import cloud.benchflow.dsl.BenchFlowExperimentAPI;
import cloud.benchflow.dsl.definition.BenchFlowExperiment;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.definition.workload.Workload;
import cloud.benchflow.dsl.demo.DemoConverter;
import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.demo.DriversMakerCompatibleID;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.FabanManagerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19
 */
public class StartTask implements Callable<Boolean> {

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

  @Override
  public Boolean call() throws Exception {

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
