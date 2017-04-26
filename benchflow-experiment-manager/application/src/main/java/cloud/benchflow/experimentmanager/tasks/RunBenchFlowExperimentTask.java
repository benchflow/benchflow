package cloud.benchflow.experimentmanager.tasks;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.DemoConverter;
import cloud.benchflow.dsl.definition.BenchFlowExperiment;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.dsl.definition.workload.Workload;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.exceptions.BenchFlowExperimentIDDoesNotExistException;
import cloud.benchflow.experimentmanager.exceptions.TrialIDDoesNotExistException;
import cloud.benchflow.experimentmanager.models.BenchFlowExperimentModel;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.ConfigFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
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
import static cloud.benchflow.faban.client.responses.RunStatus.Code.*;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         <p>
 *         Created on 07.11.16.
 */
public class RunBenchFlowExperimentTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RunBenchFlowExperimentTask.class.getSimpleName());

    private static final String TEMP_DIR = "./tmp";
    private static final String FABAN_CONFIGURATION_FILENAME = "run.xml";
    private static final String BENCHMARK_FILE_ENDING = ".jar";

    private String experimentID;

    private BenchFlowExperimentModelDAO experimentDAO;

    private MinioService minioService;
    private FabanClient fabanClient;
    private DriversMakerService driversMakerService;
    private BenchFlowTestManagerService testManagerService;
    //    private ExecutorService submitRunsPool;
    private int submitRetries = 3;


    public RunBenchFlowExperimentTask(String experimentID, BenchFlowExperimentModelDAO experimentDAO, MinioService minioService, FabanClient fabanClient, DriversMakerService driversMakerService, BenchFlowTestManagerService testManagerService, int submitRetries) {

        this.experimentID = experimentID;
        this.experimentDAO = experimentDAO;
        this.minioService = minioService;
        this.fabanClient = fabanClient;
        this.driversMakerService = driversMakerService;
        this.testManagerService = testManagerService;
        this.submitRetries = submitRetries;

    }

    @Override
    public void run() {


        try {

            // get the BenchFlowExperimentDefinition from minioService
            String experimentYamlString = IOUtils.toString(minioService.getExperimentDefinition(experimentID), StandardCharsets.UTF_8);

            BenchFlowExperiment experiment = BenchFlowDSL.experimentFromExperimentYaml(experimentYamlString);

            int nTrials = experiment.configuration().terminationCriteria().get().experiment().number();

            // inform test manager that experiment is running
            testManagerService.submitExperimentState(experimentID, BenchFlowExperimentModel.BenchFlowExperimentState.RUNNING);

            // save experiment model in DB
            experimentDAO.addExperiment(experimentID);

            // convert to old version and save to minio, and also a new experimentID to send to DriversMaker
            // generate DriversMaker compatible files on minio
            DriversMakerCompatibleID driversMakerCompatibleID = new DriversMakerCompatibleID().invoke(experimentID);
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

            for (int trialNumber = 1; trialNumber <= nTrials; trialNumber++) {

                // add trial to experiment
                String trialID = experimentDAO.addTrial(experimentID, trialNumber);

                // A) submit to fabanClient
                int retries = submitRetries;

                java.nio.file.Path fabanConfigPath = Paths.get(TEMP_DIR)
                        .resolve(experimentID)
                        .resolve(String.valueOf(trialNumber))
                        .resolve(FABAN_CONFIGURATION_FILENAME);

                InputStream configInputStream = minioService.getDriversMakerGeneratedFabanConfiguration(driversMakerExperimentID, experimentNumber, trialNumber);
                String config = IOUtils.toString(configInputStream, StandardCharsets.UTF_8);

                FileUtils.writeStringToFile(fabanConfigPath.toFile(), config, StandardCharsets.UTF_8);

                RunId runId = null;
                while (runId == null) {
                    try {

                        // TODO - should this be a method (part of Faban Client?)
                        String fabanTrialId = trialID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);

                        runId = fabanClient.submit(fabanExperimentId, fabanTrialId,
                                fabanConfigPath.toFile());

                    } catch (FabanClientException e) {
                        if (retries > 0) retries--;
                        else {
                            throw e;
                        }
                    } catch (ConfigFileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                experimentDAO.setFabanTrialID(experimentID, trialNumber, runId.toString());
                experimentDAO.setTrialModelAsStarted(experimentID, trialNumber);

                // B) wait/poll for trial to complete and store the trial result in the DB
                RunStatus status = fabanClient.status(runId); // TODO - is this the status we want to use? No it is a subset, should also include metrics computation status

                while (status.getStatus().equals(QUEUED) || status.getStatus().equals(RECEIVED) || status.getStatus().equals(STARTED)) {
                    Thread.sleep(1000);
                    status = fabanClient.status(runId);
                }

                experimentDAO.setTrialStatus(experimentID, trialNumber, status.getStatus());
                testManagerService.submitTrialStatus(trialID, status.getStatus());

            }

            experimentDAO.setExperimentModelState(experimentID, BenchFlowExperimentModel.BenchFlowExperimentState.COMPLETED);

            testManagerService.submitExperimentState(experimentID, BenchFlowExperimentModel.BenchFlowExperimentState.COMPLETED);

        } catch (IOException e) {
            logger.error("could not read experiment definition: " + e.getMessage());
            e.printStackTrace();

            // inform test manager that experiment is aborted
            testManagerService.submitExperimentState(experimentID, BenchFlowExperimentModel.BenchFlowExperimentState.ABORTED);

        } catch (RunIdNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TrialIDDoesNotExistException e) {
            e.printStackTrace();
        } catch (JarFileNotFoundException e) {
            e.printStackTrace();
        } catch (BenchFlowExperimentIDDoesNotExistException e) {
            e.printStackTrace();
        } catch (BenchFlowDeserializationException e) {
            e.printStackTrace();
        } finally {
            // remove file that was sent to fabanClient
            try {
                FileUtils.forceDelete(Paths.get(TEMP_DIR).toFile());
            } catch (IOException e) {
                // if folder doesn't exist then it is OK anyway
            }
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

    public static class DriversMakerCompatibleID {
        private String experimentName;
        private long experimentNumber;
        private String driversMakerExperimentID;

        public String getExperimentName() {
            return experimentName;
        }

        public long getExperimentNumber() {
            return experimentNumber;
        }

        public String getDriversMakerExperimentID() {
            return driversMakerExperimentID;
        }

        public DriversMakerCompatibleID invoke(String experimentID) {
            // userID = "BenchFlow"
            // ExperimentID := userId.experimentName.experimentNumber

            String[] experimentIDArray = experimentID.split(BenchFlowConstants.MODEL_ID_DELIMITER_REGEX);

            // use "-" so that it doesn't conflict with previous convention and other delimiters used
            experimentName = experimentIDArray[1] + "-" + experimentIDArray[2];
            experimentNumber = Long.parseLong(experimentIDArray[3]);
            driversMakerExperimentID = "BenchFlow." + experimentName;
            return this;
        }
    }
}
