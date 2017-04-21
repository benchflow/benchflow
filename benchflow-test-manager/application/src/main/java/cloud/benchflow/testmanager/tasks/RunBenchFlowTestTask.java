package cloud.benchflow.testmanager.tasks;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowExperiment;
import cloud.benchflow.testmanager.archive.BenchFlowTestArchiveExtractor;
import cloud.benchflow.testmanager.exceptions.BenchFlowTestIDDoesNotExistException;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 13.02.17.
 */
public class RunBenchFlowTestTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RunBenchFlowTestTask.class.getSimpleName());

    private final String testID;
    private final String testDefinitionYamlString;
    private InputStream deploymentDescriptorInputStream;
    private Map<String, InputStream> bpmnModelInputStreams;

    // services
    private final MinioService minioService;
    private final BenchFlowExperimentManagerService experimentManagerService;
    private final BenchFlowExperimentModelDAO experimentModelDAO;

    public RunBenchFlowTestTask(
            String testID,
            MinioService minioService,
            BenchFlowExperimentManagerService experimentManagerService,
            BenchFlowExperimentModelDAO experimentModelDAO,
            String testDefinitionYamlString,
            InputStream deploymentDescriptorInputStream,
            Map<String, InputStream> bpmnModelInputStreams
    ) {
        this.testID = testID;
        this.minioService = minioService;
        this.experimentManagerService = experimentManagerService;
        this.experimentModelDAO = experimentModelDAO;
        this.testDefinitionYamlString = testDefinitionYamlString;
        this.deploymentDescriptorInputStream = deploymentDescriptorInputStream;
        this.bpmnModelInputStreams = bpmnModelInputStreams;
    }

    @Override
    public void run() {

        try {

            logger.info("running test task with ID " + testID);

            // extract contents
            InputStream definitionInputStream = IOUtils.toInputStream(testDefinitionYamlString, StandardCharsets.UTF_8);

            // TODO - handle different SUT types

            // save PT archive contents to Minio
            minioService.saveTestDefinition(testID, definitionInputStream);
            minioService.saveTestDeploymentDescriptor(testID, deploymentDescriptorInputStream);

            bpmnModelInputStreams.forEach((fileName, inputStream) -> minioService.saveTestBPMNModel(testID,
                    fileName,
                    inputStream));

            // add new experiment model
            String experimentID = experimentModelDAO.addExperiment(testID);

            // generate the PE definition
            BenchFlowExperiment experiment = BenchFlowDSL.experimentFromTestYaml(testDefinitionYamlString).get();
            InputStream peDefinition = IOUtils.toInputStream(BenchFlowDSL.experimentToYamlString(experiment), StandardCharsets.UTF_8);

            // save PE defintion to minio
            minioService.saveExperimentDefinition(experimentID,
                    peDefinition);

            // save deployment descriptor + models for experiment (one bundle)
            minioService.copyDeploymentDescriptorForExperiment(testID, experimentID);
            bpmnModelInputStreams.forEach((fileName, inputStream) -> minioService.copyBPMNModelForExperiment(testID, experimentID, fileName));

            // run PE on PEManager
            experimentManagerService.runBenchFlowExperiment(experimentID);

        } catch (BenchFlowTestIDDoesNotExistException e) {
            // TODO - handle these exceptions properly (although should not happen)
            logger.error(e.toString());
        }

    }

}
