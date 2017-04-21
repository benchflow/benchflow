package cloud.benchflow.driversmaker.generation;

import cloud.benchflow.driversmaker.generation.benchflowservices.BenchFlowServices;
import cloud.benchflow.driversmaker.generation.benchflowservices.ServiceInfo;
import cloud.benchflow.driversmaker.generation.utils.RunXml;
import com.sun.faban.driver.FatalException;
import com.sun.faban.harness.*;
import com.sun.faban.driver.transport.hc3.ApacheHC3Transport;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;

import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cloud.benchflow.driversmaker.generation.utils.BenchmarkUtils;

public class BenchFlowBenchmark extends DefaultFabanBenchmark2 {

    private static Logger logger = Logger.getLogger(BenchFlowBenchmark.class.getName());

    public String deploymentManagerAddress;
    public String sutEndpoint;
    public String trialId;
    public Path benchmarkDir;
    public String privatePort;
    public BenchFlowServices benchFlowServices;
    public Map<String, ServiceInfo> serviceInfoMap;

    protected RunXml runXml;
    public ApacheHC3Transport http;

    @Configure
    public void configure() throws Exception {

        //loop on deployment manager logs

    }

    //public abstract boolean parseLog() <- returns true if log says "complete"


    private void deploySut() throws Exception {

        Path sutDir = benchmarkDir.resolve("sut");
        File dockerCompose = sutDir.resolve("docker-compose-" + trialId + ".yml").toFile();

        logger.info("[BenchFlowBenchmark] About to deploy sut from descriptor at " + dockerCompose.getAbsolutePath());

        FilePart dockerComposeFile = new FilePart("docker_compose_file", dockerCompose);

        String deployAPI = "http://" + deploymentManagerAddress + "/projects/" + trialId + "/deploymentDescriptor/";
        PutMethod put = new PutMethod(deployAPI);

        Part[] partsArray = { dockerComposeFile };
        put.setRequestEntity(new MultipartRequestEntity(partsArray, put.getParams()));

        int status = http.getHttpClient().executeMethod(put);
        logger.info("[BenchFlowBenchmark] System Deployed. Status: " + status);

    }

    private void startSut() throws Exception {

        String upAPI = "http://" + deploymentManagerAddress + "/projects/" + trialId + "/up/";
        PutMethod putUp = new PutMethod(upAPI);
        int statusUp = http.getHttpClient().executeMethod(putUp);

        logger.info("[BenchFlowBenchmark] System Started. Status:" + statusUp);

    }


    /**
     * This method sets sut endpoint, deployment manager address, trial id
     */
    @Override
    @Validate
    public void validate() throws Exception {
        super.validate();

        logger.info("[BenchFlowBenchmark] START: Validate...");

        initialize();

        logger.info("[BenchFlowBenchmark] DONE: initialize");
        logger.info("[BenchFlowBenchmark] benchmarkDir is: " + benchmarkDir.toString());

        moveBenchFlowServicesConfigToProperties();

        logger.info("[BenchFlowBenchmark] Moved BenchFlow section of config file to driver's properties");

        deploymentManagerAddress = runXml.getXPathValue("benchFlowServices/deploymentManager");
        logger.info("[BenchFlowBenchmark] Deployment manager address is: " + deploymentManagerAddress);

        trialId = runXml.getXPathValue("benchFlowRunConfiguration/trialId");
        logger.info("[BenchFlowBenchmark] Trial id is: " + trialId);

        //we do the complete deployment in @Validate phase since
        //it is the only phase in which we can modify the run xml to
        //include the sut endpoint
        deploySut();
        startSut();
        setSutEndpoint();

        benchFlowServices = new BenchFlowServices(
                serviceInfoMap,
                deploymentManagerAddress,
                privatePort,
                trialId,
                http.getHttpClient(),
                logger
        );

        logger.info("[BenchFlowBenchmark] DONE: setSutEndpoint");
        logger.info("[BenchFlowBenchmark] END: Validate...");

    }

    @StartRun
    public void start() throws Exception {
        logger.info("[BenchFlowBenchmark] About to start BenchFlow services...");
        try {
            benchFlowServices.start();
        } catch(Exception e) {
            logger.severe("An error occurred while starting BenchFlow services");
            logger.log(Level.SEVERE, e.getMessage(), e);
            //the fatal exception ensures that the benchmark stops if there is an error in this phase
            throw new FatalException("An error occurred while starting BenchFlow services");
        }
        logger.info("[BenchFlowBenchmark] BenchFlow services started.");
        logger.info("[BenchFlowBenchmark] Calling super.start");
        super.start();
    }

    /**
     * Undeploys the sut
     */
    protected int undeploySut() throws Exception {
        //remove the sut
        //curl -v -X PUT http://<HOST_IP>:<HOST_PORT>/projects/camunda/rm/
        String rmAPI = "http://" + deploymentManagerAddress + "/projects/" + trialId + "/rm/";
        PutMethod putRm = new PutMethod(rmAPI);
        int statusRm = http.getHttpClient().executeMethod(putRm);
        logger.info("[BenchFlowBenchmark] Sut undeploy requested. Response status: " + statusRm);
        return statusRm;
    }


    /**
     * Undeploys the sut
     */
    @PostRun
    public void postRun() throws Exception {
        undeploySut();
    }


    @Override
    @EndRun
    public void end() throws Exception {
        try {
            super.end();
        } catch (Exception e) {
            undeploySut();
        }
    }


    @KillRun
    public void kill() throws Exception {
        undeploySut();
    }


    /**
     * Retrieves sut endpoint address from deployment manager and sets resolved address as field of this class
     */
    private void setSutEndpoint() throws Exception {

        StringBuilder urlBuilder = new StringBuilder();

        String targetServiceName = runXml.getXPathValue("sutConfiguration/serviceName");
        logger.info("[BenchFlowBenchmark] Target service name is: " + targetServiceName);

        String targetServiceEndpoint = runXml.getXPathValue("sutConfiguration/endpoint");
        logger.info("[BenchFlowBenchmark] Target service endpoint is: " + targetServiceEndpoint);

        privatePort = runXml.getXPathValue("benchFlowServices/privatePort");

        logger.info("[BenchFlowBenchmark] Private port is: " + privatePort);

        logger.info("[BenchFlowBenchmark] Checking configuration for sut endpoint address");
        String sutAddress = runXml.getXPathValue("sutConfiguration/address");

        if(sutAddress != null) {

            logger.info("[BenchFlowBenchmark] Sut endpoint address found");

        } else {

            logger.info("[BenchFlowBenchmark] Sut address not found in config file, " +
                        "about to retrieve target service address from " +
                        "deployment manager");

            sutAddress = BenchmarkUtils.benchFlowServiceAddress(
                    deploymentManagerAddress,
                    privatePort,
                    targetServiceName,
                    trialId,
                    http.getHttpClient());

        }


        logger.info("[BenchFlowBenchmark] Successfully retrieved target service address: " + sutAddress);

        sutEndpoint = urlBuilder.append("http://")
                .append(sutAddress)
                .append(targetServiceEndpoint).toString();

        logger.info("[BenchFlowBenchmark] SUT endpoint is: " + sutEndpoint);

        runXml.addConfigurationNode("sutConfiguration", "sutEndpoint", sutEndpoint);
        logger.info("[BenchFlowBenchmark] Added sutEndpoint node to configuration");
    }


    /**
     * Setup benchmarkDir, http transport, and services info map
     */
    protected void initialize() throws Exception {
        this.benchmarkDir = Paths.get(RunContext.getBenchmarkDir());
        this.http = new ApacheHC3Transport();
        this.runXml = new RunXml(params, logger);
    }


    /**
     * Moves benchFlowServices section of run.xml to first driver properties
     */
    protected void moveBenchFlowServicesConfigToProperties() throws Exception {

        Document runDoc = params.getTopLevelElements().item(0).getOwnerDocument();

        String driverConfigNodeXPath = "fa:runConfig/fd:driverConfig[1]";
        Element driverConfigNode = (Element) runXml.getNode(driverConfigNodeXPath);
        logger.info("[BenchFlowBenchmark] Node for first driver exists: " + String.valueOf(driverConfigNode != null));

        Element driverProperties = (Element) runXml.getNode("properties", driverConfigNode);
        logger.info("[BenchFlowBenchmark] Properties node for first driver exists: " + String.valueOf(driverProperties != null));

        Element benchFlowServicesConfiguration = (Element) runXml.getNode("benchFlowServices/servicesConfiguration");
        logger.info("[BenchFlowBenchmark] servicesConfiguration node exists: " + String.valueOf(benchFlowServicesConfiguration != null));

        if (driverProperties == null) {
            logger.info("[BenchFlowBenchmark] Adding properties node for first driver");
            driverProperties = runXml.addConfigurationNode(driverConfigNodeXPath, "properties", "");
        }

        boolean omitDeclaration = true;
        boolean prettyPrint = false;

        //serialize benchFlowServices node to string
        String serializedBenchFlowServicesConfiguration =
                BenchmarkUtils.nodeToString(benchFlowServicesConfiguration, omitDeclaration, prettyPrint);

        //escape it
        serializedBenchFlowServicesConfiguration = StringEscapeUtils.escapeXml10(serializedBenchFlowServicesConfiguration);

        //I have to parse the uglified node in order to avoid having to parse \n
        serviceInfoMap = BenchmarkUtils.parseBenchmarkConfiguration(
            BenchmarkUtils.stringToNode(StringEscapeUtils.unescapeXml(serializedBenchFlowServicesConfiguration))
        );

        logger.info("[BenchFlowBenchmark] Successfully parsed benchmark configuration");

        Element servicesConfigurationProperty = runXml.addConfigurationNode(driverProperties, "property", "");
        servicesConfigurationProperty.setAttribute("name", "servicesConfiguration");
        servicesConfigurationProperty.appendChild(runDoc.createTextNode(serializedBenchFlowServicesConfiguration));

        driverProperties.appendChild(servicesConfigurationProperty);

        runXml.save();

    }

}