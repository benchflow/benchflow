package cloud.benchflow.driversmaker.generation.benchflowservices;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 26/07/16.
 */
public class ServiceInfo {

    private String serviceName;
    private List<CollectorInfo> collectors;

    public ServiceInfo(String name) {
        collectors = new ArrayList<CollectorInfo>();
        serviceName = name;
    }

    public void addCollector(CollectorInfo collector) {
        collectors.add(collector);
    }

    public String getName() {
        return serviceName;
    }

    public List<CollectorInfo> getCollectors() {
        return collectors;
    }

}
