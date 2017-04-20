package cloud.benchflow.testmanager.tasks.running;

import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.tasks.BenchFlowTestTaskController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-20
 */
public class HandleExperimentResultTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(HandleExperimentResultTask.class.getSimpleName());

    private final String experimentID;
    private final BenchFlowTestTaskController testTaskController;

    public HandleExperimentResultTask(String experimentID) {
        this.experimentID = experimentID;
        this.testTaskController = BenchFlowTestManagerApplication.getTestTaskController();
    }

    @Override
    public void run() {

        logger.info("Handling experiment result: " + experimentID);

        // TODO - get the experiment result

        // TODO - decide next step (run another experiment or terminate)

    }
}
