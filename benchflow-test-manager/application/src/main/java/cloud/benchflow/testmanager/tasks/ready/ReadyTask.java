package cloud.benchflow.testmanager.tasks.ready;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.tasks.BenchFlowTestTaskController;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-20
 */
public class ReadyTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ReadyTask.class.getSimpleName());

    private final String testID;
    private final String testDefinitionYamlString;
    private final InputStream deploymentDescriptorInputStream;
    private final Map<String, InputStream> bpmnModelInputStreams;

    // services
    private final MinioService minioService;

    private final BenchFlowTestTaskController taskController;


    public ReadyTask(String testID, String testDefinitionYamlString, InputStream deploymentDescriptorInputStream, Map<String, InputStream> bpmnModelInputStreams) {

        this.testID = testID;
        this.testDefinitionYamlString = testDefinitionYamlString;
        this.deploymentDescriptorInputStream = deploymentDescriptorInputStream;
        this.bpmnModelInputStreams = bpmnModelInputStreams;

        this.taskController = BenchFlowTestManagerApplication.getTestTaskController();
        this.minioService = BenchFlowTestManagerApplication.getMinioService();
    }

    @Override
    public void run() {

        logger.info("preparing test with ID " + testID);

        // extract contents
        InputStream definitionInputStream = IOUtils.toInputStream(testDefinitionYamlString, StandardCharsets.UTF_8);

        // TODO - handle different SUT types

        // save PT archive contents to Minio
        minioService.saveTestDefinition(testID, definitionInputStream);
        minioService.saveTestDeploymentDescriptor(testID, deploymentDescriptorInputStream);

        bpmnModelInputStreams.forEach((fileName, inputStream) -> minioService.saveTestBPMNModel(testID,
                fileName,
                inputStream));

        taskController.runDetermineExecuteExperimentsTask(testID);

    }


}
