package cloud.benchflow.experiment.drivers;

import cloud.benchflow.driversmaker.generation.BenchFlowDriver;

import java.util.*;


public class Driver extends BenchFlowDriver {

    private Map<String,String> modelsStartID;

    //add plugin with spoon


    public Driver() throws Exception {
        super();
        modelsStartID = new HashMap<String, String>();
        loadModelsInfo();
    }


    private void loadModelsInfo() {
        int numModel = Integer.parseInt(getContextProperty("model_num"));
        for (int i = 1; i <= numModel; i++) {
            String name = getContextProperty("model_" + i + "_name");
            String startID = getContextProperty("model_" + i + "_startID");
            modelsStartID.put(name, startID);
        }
    }


    protected void initialize() throws Exception {
        super.initialize();
        modelsStartID = new HashMap<String,String>();
    }

}