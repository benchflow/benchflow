package cloud.benchflow.driversmaker.utils.env;

import cloud.benchflow.experiment.GenerationDefaults;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 03/03/16.
 */
public class DriversMakerEnv /*extends BenchFlowEnv*/ {

    private String benchFlowServicesPath;
    private String generationResourcesPath;
    private ConfigYml configYml;
    private GenerationDefaults heuristics;
    private String privatePort;

    public DriversMakerEnv(/*String configPath,*/
                           ConfigYml configYml,
                           String benchFlowServicesPath,
                           String generationResourcesPath,
                           String privatePort) {
        //super(configPath);
        this.configYml = configYml;
        this.benchFlowServicesPath = benchFlowServicesPath;
        this.generationResourcesPath = generationResourcesPath;
        this.heuristics = new GenerationDefaults(configYml);
        this.privatePort = privatePort;
    }

    public String getBenchFlowServicesPath() {
        return benchFlowServicesPath;
    }

    public ConfigYml getConfigYml() {
        return this.configYml;
    }

    public GenerationDefaults getHeuristics() {
        return this.heuristics;
    }

    public String getDeploymentManagerAddress() {
        return this.configYml.<String>getVariable("BENCHFLOW_DEPLOYMENT_MANAGER_ADDRESS");
    }

    public String getEnvConsulAddress() {
        return this.configYml.<String>getVariable("BENCHFLOW_ENVCONSUL_CONSUL_ADDRESS");
    }

    public String getHostname(String serverAlias) {
        return this.configYml.<String>getVariable("BENCHFLOW_SERVER_" + serverAlias.toUpperCase() + "_HOSTNAME");
    }

    public String getGenerationResourcesPath() {
        return generationResourcesPath;
    }

    public void setGenerationResourcesPath(String generationResourcesPath) {
        this.generationResourcesPath = generationResourcesPath;
    }

    public String getPrivatePort() {
        return privatePort;
    }

    public String getPublicIp(String serverAlias) {
        return configYml.getVariable("BENCHFLOW_SERVER_" + serverAlias.toUpperCase() + "_PUBLICIP");
    }

    public String getLocalIp(String serverAlias) {
        return configYml.getVariable("BENCHFLOW_SERVER_" + serverAlias.toUpperCase() + "_PRIVATEIP");
    }

    public String getIp(String serverAlias) {
        String privateIp = getLocalIp(serverAlias);
        if(privateIp == null) {
            return getPublicIp(serverAlias);
        }
        return privateIp;
    }
}