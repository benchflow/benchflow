package cloud.benchflow.driversmaker.generation.benchflowservices;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 26/07/16.
 */
public class CollectorInfo {

    private String collectorName;
    private String collectorId;
    private List<MonitorInfo> monitors;
    private String startAPI;
    private String stopAPI;
    private String address;

    public CollectorInfo(String name, String id) {
        monitors = new ArrayList<MonitorInfo>();
        collectorName = name;
        collectorId = id;
    }

    public String getName() {
        return collectorName;
    }

    public String getId() {
        return collectorId;
    }

    public List<MonitorInfo> getMonitors() {
        return monitors;
    }

    public String getStartAPI() {
        return startAPI;
    }

    public String getStopAPI() {
        return stopAPI;
    }

    public void setStartAPI(String start) {
        startAPI = start;
    }

    public void setStopAPI(String stop) {
        stopAPI = stop;
    }

    public void addMonitor(MonitorInfo monitor) {
        monitors.add(monitor);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
