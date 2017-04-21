package cloud.benchflow.driversmaker.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 13/02/16.
 */
public class BenchFlowEnvConfiguration {

    @NotEmpty
    private String configPath;

    @NotEmpty
    private String benchFlowServicesPath;

    @NotEmpty
    private String generationResourcesPath;

    @NotEmpty
    private String privatePort;

    @JsonProperty("config.yml")
    public String getConfigPath() {
        return configPath;
    }

    @JsonProperty("config.yml")
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    @JsonProperty("benchflowServices")
    public String getBenchFlowServicesPath() {
        return benchFlowServicesPath;
    }

    @JsonProperty("benchflowServices")
    public void setBenchFlowServicesPath(String benchFlowServicesPath) {
        this.benchFlowServicesPath = benchFlowServicesPath;
    }

    @JsonProperty("generationResources")
    public String getGenerationResourcesPath() {
        return generationResourcesPath;
    }

    @JsonProperty("generationResources")
    public void setGenerationResourcesPath(String generationResourcesPath) {
        this.generationResourcesPath = generationResourcesPath;
    }

    @JsonProperty("privatePort")
    public String getPrivatePort() {
        return privatePort;
    }

    @JsonProperty("privatePort")
    public void setPrivatePort(String privatePort) {
        this.privatePort = privatePort;
    }
}
