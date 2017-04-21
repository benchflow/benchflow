package cloud.benchflow.experiment.harness;

import cloud.benchflow.driversmaker.generation.BenchFlowBenchmark;

import com.sun.faban.harness.*;

import java.util.*;
import java.util.logging.Logger;

import java.io.File;
import java.nio.file.Path;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WfMSBenchmark extends BenchFlowBenchmark {

    private static Logger logger = Logger.getLogger(WfMSBenchmark.class.getName());

    private Map<String, String> modelsStartID;

    //override validate instead of initialize?
//    protected void initialize() throws Exception {
//        super.initialize();
//        logger.info("About to initialize wfms plugin...");
//        //generate with spoon
//        //plugin = new WfMSPlugin(...)
//    }
    public void initialize() throws Exception {
        super.initialize();
        modelsStartID = new HashMap<String, String>();
    }

    @Configure
    public void configure() throws Exception {
        super.configure();
        logger.info("About to configure wfms plugin...");
        //generate with spoon
        //plugin = new WfMSPlugin(sutEndpoint);
    }

    public void addModel(Element properties, int modelNum, String modelName, String processDefinitionId) throws Exception {
        //We need to attach them as driver properties otherwise it is not possible to access them in the Driver
        //Add the information about the deployed process in the run context
        //TODO: provide abstracted method to improve the adding of informations like the following, dinamically
        //Maybe also improving com.sun.faban.harness.ParamRepository if needed
        /**
         * <models>
         *  <model id="processDefinitionId">
         *   <name></name>
         *   <startID></startID>
         *  </model>
         * </models>
         */
        runXml.addProperty(properties, "model_" + modelNum + "_name", modelName);
        runXml.addProperty(properties, "model_" + modelNum + "_startID", processDefinitionId);
    }

    /**
     *  Deploys BPMN models
     */
    @PreRun
    public void preRun() throws Exception {

        logger.info("START: Deploying processes...");
//        logger.info("WfMSBenchmark preRun");
//        logger.info("Sut Endpoint is: " + sutEndpoint);

        int numDeplProcesses = 0;
        Path modelDir = benchmarkDir.resolve("models");

        File[] listOfModels = modelDir.toFile().listFiles();

        //Add models node
        String agentName = "WfMSStartDriver";
        String driverToUpdate = "fa:runConfig/fd:driverConfig[@name=\"" + agentName + "\"]";

        Element properties = (Element) runXml.getNode(driverToUpdate + "/properties");

        if(properties == null) {
            logger.info("Adding properties node for driver WfMSStartDriver");
            properties = runXml.addConfigurationNode(driverToUpdate,"properties","");
        }

        for (int i = 0; i < listOfModels.length; i++) {
            if (listOfModels[i].isFile()) {
                String modelName = listOfModels[i].getName();

                //logger.info("Found model " + modelName);

                String modelPath = modelDir + "/" + modelName;
                File modelFile = new File(modelPath);
                String processDefinitionId = null;

                //logger.info("About to deploy model " + modelName);

                //add with spoon
                //processDefinitionId = plugin.deploy(modelFile).get(modelName);

                addModel(properties, i+1, modelName,processDefinitionId);
                numDeplProcesses++;
                logger.info("PROCESS DEFINITION ID: " + processDefinitionId);
            }
        }

        runXml.addProperty(properties, "model_num", String.valueOf(numDeplProcesses));
        logger.info("END: Deploying processes...");
    }

}