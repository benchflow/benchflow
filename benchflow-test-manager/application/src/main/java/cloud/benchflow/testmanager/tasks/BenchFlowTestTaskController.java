package cloud.benchflow.testmanager.tasks;

import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import cloud.benchflow.testmanager.tasks.start.StartTask;
import cloud.benchflow.testmanager.tasks.running.DetermineExecuteExperimentsTask;
import cloud.benchflow.testmanager.tasks.running.HandleExperimentResultTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-20
 */
public class BenchFlowTestTaskController {

    private static Logger logger = LoggerFactory.getLogger(BenchFlowTestTaskController.class.getSimpleName());

    private ConcurrentMap<String, Runnable> testTasks = new ConcurrentHashMap<>();

    private ExecutorService taskExecutorService;

    public BenchFlowTestTaskController(ExecutorService taskExecutorService) {
        this.taskExecutorService = taskExecutorService;
    }

    // used for testing
    protected ExecutorService getTaskExecutorService() {
        return taskExecutorService;
    }

    synchronized public void startTest(String testID, String testDefinitionYamlString, InputStream deploymentDescriptorInputStream, Map<String, InputStream> bpmnModelInputStreams) {

        logger.info("startTest with testID: " + testID);

        if (testStarted(testID)) return;

        StartTask startTask = new StartTask(
                testID,
                testDefinitionYamlString,
                deploymentDescriptorInputStream,
                bpmnModelInputStreams
        );


        // TODO - change to future and then decide what to do next here
        testTasks.put(testID, startTask);

        // TODO - should go into a stateless queue (so that we can recover)
        taskExecutorService.submit(startTask);

    }

    synchronized public void runDetermineExecuteExperimentsTask(String testID) {

        logger.info("runDetermineExecuteExperimentsTask with testID: " + testID);

        if (!testStarted(testID)) return;

        DetermineExecuteExperimentsTask task = new DetermineExecuteExperimentsTask(testID);

        // replace with new task
        testTasks.put(testID, task);

        // TODO - should go into a stateless queue (so that we can recover)
        taskExecutorService.submit(task);

    }

    synchronized public void handleExperimentResult(String experimentID) {

        logger.info("handleExperimentResult for experimentID: " + experimentID);

        String testID = BenchFlowConstants.getTestIDFromExperimentID(experimentID);

        if (!testStarted(testID)) return;

        HandleExperimentResultTask task = new HandleExperimentResultTask(experimentID);

        // replace with new task
        testTasks.put(testID, task);

        // TODO - should go into a stateless queue (so that we can recover)
        taskExecutorService.submit(task);

    }

    synchronized public void testMaxTimeReached(String testID) {

        logger.info("testMaxTimeReached: " + testID);

        // TODO - implement me

    }

    private boolean testStarted(String testID) {
        if (testTasks.containsKey(testID)) {
            logger.info("test already submitted");
            return true;
        }
        return false;
    }


}
