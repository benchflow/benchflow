package cloud.benchflow.driversmaker.generation.benchflowservices;

import java.util.Map;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 26/07/16.
 */
public class MonitorInfo {

    private String monitorName;
    private String monitorId;

    private String startAPI;
    private String monitorAPI;
    private String stopAPI;

    private String runPhase;
    private Map<String, String> params;

    public MonitorInfo(String name, String id) {
        monitorName = name;
        monitorId = id;
    }

    public String getName() {
        return monitorName;
    }

    public String getId() {
        return monitorId;
    }

    public String getStartAPI() {
        return startAPI;
    }

    public String getStopAPI() {
        return stopAPI;
    }

    public String getMonitorAPI() {
        return monitorAPI;
    }

    public void setStartAPI(String start) {
        startAPI = start;
    }

    public void setStopAPI(String stop) {
        stopAPI = stop;
    }

    public void setMonitorAPI(String monitor) {
        monitorAPI = monitor;
    }

    public String getRunPhase() {
        return runPhase;
    }

    public void setRunPhase(String phase) {
        runPhase = phase;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> p) {
        params = p;
    }

}
