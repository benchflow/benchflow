package cloud.benchflow.driversmaker.generation;

import cloud.benchflow.driversmaker.generation.benchflowservices.*;
import cloud.benchflow.driversmaker.generation.utils.BenchmarkUtils;

import com.sun.faban.driver.*;
import com.sun.faban.driver.transport.hc3.ApacheHC3Transport;

import org.apache.commons.lang3.StringEscapeUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 25/07/16.
 *
 * Base BenchFlow driver. Handles monitors and collectors lifecycle
 */
public class BenchFlowDriver {

    protected DriverContext ctx;
    protected ApacheHC3Transport http;
    protected String sutEndpoint;
    protected String trialId;
    

    private Logger logger;
    private String deploymentManagerAddress;
    private BenchFlowServices benchFlowServices;
    private String privatePort;

    public BenchFlowDriver() throws Exception {
        initialize();
        setSutEndpoint();
        configure();
    }

    public void configure() {
        //this can be overridden in subclasses
        //it's useful to configure with the assumption that everything else
        //has been set before this
    }

    protected String getContextProperty(String property){
        return ctx.getProperty(property);
    }

    protected String getXPathValue(String xPathExpression) throws Exception {
        return ctx.getXPathValue(xPathExpression);
    }


    @OnceBefore
    public void onceBefore() throws Exception {
        //We wait a bit to create a gap in the data (TODO-RM: experimenting with data cleaning)
        //and be sure the model started during the warm up and timing synch of the sistem, end,
        //event though now that we use mock models they end very fast
        Thread.sleep(20000);
        logger.info("[BenchFlowDriver] Tested pre-run (sleep 20) done");
//        logger.info("[BenchFlowDriver] About to start BenchFlow services");
//        benchFlowServices.start();
    }


    //if monitor == end -> start, monitor, stop
    //if monitor == all -> monitor, stop
    //collector -> stop
    @OnceAfter
    public void onceAfter() throws Exception {
        logger.info("[BenchFlowDriver] About to stop BenchFlow services");
        try {
            benchFlowServices.stop();
        } catch(Exception e) {
            logger.severe("An error occurred while stopping BenchFlow services");
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new FatalException("An error occurred while stopping BenchFlow services");
        }
        logger.info("[BenchFlowDriver] BenchFlow services successfully stopped");
    }


    private void setSutEndpoint() throws Exception {
        sutEndpoint = getXPathValue("sutConfiguration/sutEndpoint");
    }

    protected boolean isStarted() {

        long steadyStateStartTime = ctx.getSteadyStateStartNanos();
        //If we don't have the steadyStateStartTime, it means it is not yet set,
        //then we are not during the run
        if(steadyStateStartTime !=0){

            long rampUpTime = ctx.getRampUp() * 1000000000l;
            long steadyStateTime = ctx.getSteadyState() * 1000000000l;
            long rampDownTime = ctx.getRampDown() * 1000000000l;

            long rampUpStartTime = steadyStateStartTime - rampUpTime;
            long steadyStateEndTime = steadyStateStartTime + steadyStateTime;
            long rampDownEndTime = steadyStateEndTime + rampDownTime;

            long currentTime = ctx.getNanoTime();

            //logger.info("rampUpTime: " + rampUpTime);
            //logger.info("steadyStateTime: " + steadyStateTime);
            //logger.info("rampDownTime: " + rampDownTime);
            //logger.info("rampUpStartTime: " + rampUpStartTime);
            //logger.info("steadyStateEndTime: " + steadyStateEndTime);
            //logger.info("rampDownEndTime: " + rampDownEndTime);
            //logger.info("steadyStateStartTime: " + steadyStateStartTime);
            //logger.info("currentTime: " + currentTime);

            return (rampUpStartTime <= currentTime) && (currentTime <= rampDownEndTime);
        }

        return false;
    }

    protected void initialize() throws Exception {

        ctx = DriverContext.getContext();
        logger = ctx.getLogger();

        HttpTransport.setProvider("com.sun.faban.driver.transport.hc3.ApacheHC3Transport");
        http = (ApacheHC3Transport) HttpTransport.newInstance();

        trialId = getXPathValue("benchFlowRunConfiguration/trialId");

        logger.info("[BenchFlowDriver] Trial id is: " + trialId);

        deploymentManagerAddress = getXPathValue("benchFlowServices/deploymentManager");

        logger.info("[BenchFlowDriver] Deployment manager address is: " + deploymentManagerAddress);

        privatePort = getXPathValue("benchFlowServices/privatePort");

        logger.info("[BenchFlowDriver] Private port is: " + privatePort);




        Map<String, ServiceInfo> serviceInfoMap = parseBenchmarkConfiguration();
        benchFlowServices = new BenchFlowServices(serviceInfoMap,
                                                  deploymentManagerAddress,
                                                  privatePort,
                                                  trialId,
                                                  http.getHttpClient(),
                                                  logger);
    }

    /**
     * Parse run.xml to build a service info map
     */
    private Map<String, ServiceInfo> parseBenchmarkConfiguration() throws Exception {

        Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

        logger.info("[BenchFlowDriver] About to parse serialized BenchFlow services configuration");

        String benchFlowServicesSerializedNode = getContextProperty("servicesConfiguration");
        benchFlowServicesSerializedNode = StringEscapeUtils.unescapeXml(benchFlowServicesSerializedNode);
        Node benchFlowServices = BenchmarkUtils.stringToNode(benchFlowServicesSerializedNode);

        logger.info("[BenchFlowDriver] Successfully parsed BenchFlow services configuration");

        return BenchmarkUtils.parseBenchmarkConfiguration(benchFlowServices);

    }

}
