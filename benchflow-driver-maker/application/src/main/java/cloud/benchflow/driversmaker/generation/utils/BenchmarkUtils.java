package cloud.benchflow.driversmaker.generation.utils;

import cloud.benchflow.driversmaker.generation.benchflowservices.CollectorInfo;
import cloud.benchflow.driversmaker.generation.benchflowservices.MonitorInfo;
import cloud.benchflow.driversmaker.generation.benchflowservices.ServiceInfo;
import cloud.benchflow.driversmaker.generation.utils.exceptions.DeploymentManagerException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 24/07/16.
 */
public class BenchmarkUtils {

    private static Logger logger = Logger.getLogger(BenchmarkUtils.class.getName());
    private static int DEPLOYMENT_MANAGER_TRIALS = 3;

    /***
     * Makes a request to url, and retries for DEPLOYMENT_MANAGER_TRIALS
     * in case of failure
     */
    private static String makeDeploymentManagerRequest(String url, HttpClient http)
        throws DeploymentManagerException {

        int trials = 0;
        String response = null;

        while(trials <= DEPLOYMENT_MANAGER_TRIALS) {

            logger.info("About to make request to deployment manager: " + url);

//            HttpClient http = new HttpClient();
            HttpMethod getRequest = new GetMethod(url);
            try {

                trials++;
                http.executeMethod(getRequest);
                response = new String(getRequest.getResponseBody(), "UTF-8");
                getRequest.releaseConnection();
                return response;

            } catch (Exception e) {

                logger.info("Trial " + trials + " for request " + url + " failed. " +
                            "See the error message in the following log.");
                logger.log(Level.INFO, e.getMessage(), e);

            }
        }

        throw new DeploymentManagerException(
                "All trials for request " + url + " failed. " +
                "See the logs for additional information."
        );

    }


    /**
     * Retrieves the address for a benchflow service from the deployment manager
     */
    public static String benchFlowServiceAddress(String deploymentManagerAddress,
                                                 String privatePort,
                                                 String benchFlowServiceId,
                                                 String trialId,
                                                 HttpClient http)
        throws DeploymentManagerException {

        String deploymentManagerPortsApi = "http://" + deploymentManagerAddress + "/projects/" +
                                            trialId + "/port/" + benchFlowServiceId + "/" + privatePort;

        String serviceAddress = makeDeploymentManagerRequest(deploymentManagerPortsApi, http);

        logger.info("Final computed sut address for " + benchFlowServiceId + " " +
                    "http://" + serviceAddress.trim().replaceAll("\\s+", ""));


        return "http://" + serviceAddress.trim().replaceAll("\\s+", "");

    }

    /**
     * Takes a node and serializes it to string
     */
    public static String nodeToString(Node node, boolean omitDeclaration, boolean prettyPrint) {

        try {

            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression xpathExpr = xpath.compile("//text()[normalize-space()='']");
            NodeList nodeList = (NodeList) xpathExpr.evaluate(node, XPathConstants.NODESET);

            for(int i = 0; i < nodeList.getLength(); i++) {
                Node nd = nodeList.item(i);
                nd.getParentNode().removeChild(nd);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.VERSION, "1.1");

            if(omitDeclaration) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }

            Writer writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();

        } catch (XPathExpressionException e) {
            throw new RuntimeException("An error occurred while serializing the xml node");
        } catch (TransformerException e) {
            throw new RuntimeException("An error occurred while serializing the xml node");
        }

    }

    /**
     * Takes an xml string (with declaration) and return the parsed node
     */
    public static Node stringToNode(String nodeString) {

        try {

            return DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(nodeString.getBytes("utf-8")))
                    .getDocumentElement();

        } catch (SAXException e) {
            throw new RuntimeException("An error occurred while parsing the xml node");
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while parsing the xml node");
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("An error occurred while parsing the xml node");
        }

    }

    /**
     * Parse run.xml to build a service info map
     */
    public static Map<String, ServiceInfo> parseBenchmarkConfiguration(Node benchFlowServices) throws Exception {

        Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

        logger.info("[BenchmarkUtils] Successfully parsed BenchFlow services configuration");

        NodeList services = benchFlowServices.getChildNodes();

        //iterate over every service in the configuration
        for(int serviceIndex = 0; serviceIndex < services.getLength(); serviceIndex++) {

            Element service = (Element) services.item(serviceIndex);
            String serviceName = service.getAttribute("name");

            logger.info("[BenchmarkUtils] Parsing xml node for service " + serviceName);

            ServiceInfo serviceInfo = new ServiceInfo(serviceName);

            NodeList collectors = service.getElementsByTagName("collector");

            //iterate over all collectors for the given service
            for(int collectorIndex = 0; collectorIndex < collectors.getLength(); collectorIndex++) {

                Element collector = (Element) collectors.item(collectorIndex);
                String collectorName = collector.getAttribute("name");

                logger.info("[BenchmarkUtils] Parsing xml node for collector " + collectorName + " of service " + serviceName);

                String collectorId = collector.getElementsByTagName("id").item(0).getTextContent();

                logger.info("[BenchmarkUtils] Collector id is: " + collectorId);

                CollectorInfo collectorInfo = new CollectorInfo(collectorName, collectorId);

                Element apiNode = (Element) collector.getElementsByTagName("api").item(0);

                //find out start and stop API values
                NodeList collectorAPIs = apiNode.getChildNodes();
                for(int apiIndex = 0; apiIndex < collectorAPIs.getLength(); apiIndex++) {

                    Element currentAPI = (Element) collectorAPIs.item(apiIndex);

                    if(currentAPI.getTagName().equals("start")) {

                        collectorInfo.setStartAPI(currentAPI.getTextContent());
                        logger.info("[BenchmarkUtils] Start API for collector " + collectorName + " is "
                                + collectorInfo.getStartAPI());

                    } else if(currentAPI.getTagName().equals("stop")) {

                        collectorInfo.setStopAPI(currentAPI.getTextContent());
                        logger.info("[BenchmarkUtils] Stop API for collector " + collectorName + " is "
                                + collectorInfo.getStopAPI());

                    }

                }

                NodeList addressNode = collector.getElementsByTagName("address");
                //the collector has network host, so we saved the address in the run.xml
                if(addressNode.getLength() != 0) {
                    logger.info("[BenchmarkUtils] Collector " + collectorName + " has network_mode: host, " +
                            "retrieving address");
                    String collectorAddress = ((Element) addressNode.item(0)).getTextContent();
                    collectorInfo.setAddress(collectorAddress);
                }

                NodeList monitors = collector.getElementsByTagName("monitors").item(0).getChildNodes();

                for(int monitorIndex = 0; monitorIndex < monitors.getLength(); monitorIndex++) {

                    Element monitor = (Element) monitors.item(monitorIndex);
                    String monitorName = monitor.getAttribute("name");

                    logger.info("[BenchmarkUtils] Parsing xml node for monitor " + monitorName + " of collector "
                            + collectorName + " of service " + serviceName);

                    String monitorId = monitor.getElementsByTagName("id").item(0).getTextContent();

                    logger.info("[BenchmarkUtils] Monitor id is: " + monitorId);

                    MonitorInfo monitorInfo = new MonitorInfo(monitorName, monitorId);

                    //build monitor parameters map
                    Element monitorConfiguration = (Element) monitor.getElementsByTagName("configuration").item(0);
                    NodeList monitorConfigurationParams = monitorConfiguration.getElementsByTagName("param");
                    Map<String, String> params = new HashMap<String, String>();
                    for(int paramIndex = 0; paramIndex < monitorConfigurationParams.getLength(); paramIndex++) {

                        Element param = (Element) monitorConfigurationParams.item(paramIndex);
                        String paramName = param.getAttribute("name");

                        logger.info("[BenchmarkUtils] Found param " + paramName + " for monitor " + monitorName);

                        String paramValue = param.getTextContent();

                        logger.info("[BenchmarkUtils] Parameter " + paramName + " has value " + paramValue);

                        params.put(paramName, paramValue);

                    }

                    monitorInfo.setParams(params);

                    Element monitorApiNode = (Element) monitor.getElementsByTagName("api").item(0);

                    //find out monitor APIs
                    NodeList monitorAPIs = monitorApiNode.getChildNodes();
                    for(int monitorApiIndex = 0; monitorApiIndex < monitorAPIs.getLength(); monitorApiIndex++) {

                        Element currentMonitorAPI = (Element) monitorAPIs.item(monitorApiIndex);
                        if(currentMonitorAPI.getTagName().equals("start")) {

                            monitorInfo.setStartAPI(currentMonitorAPI.getTextContent());
                            logger.info("[BenchmarkUtils] Start API for monitor " + monitorName + " is " + monitorInfo.getStartAPI());

                        } else if(currentMonitorAPI.getTagName().equals("monitor")) {

                            monitorInfo.setMonitorAPI(currentMonitorAPI.getTextContent());
                            logger.info("[BenchmarkUtils] Monitor API for monitor " + monitorName + " is " + monitorInfo.getMonitorAPI());

                        } else if(currentMonitorAPI.getTagName().equals("stop")) {

                            monitorInfo.setStopAPI(currentMonitorAPI.getTextContent());
                            logger.info("[BenchmarkUtils] Stop API for monitor " + monitorName + " is " + monitorInfo.getStopAPI());

                        }

                    }

                    //check phase
                    String runPhase = monitor.getElementsByTagName("runPhase").item(0).getTextContent();
                    monitorInfo.setRunPhase(runPhase);
                    logger.info("[BenchmarkUtils] Run phase for monitor " + monitorName + " is " + runPhase);

                    collectorInfo.addMonitor(monitorInfo);
                }

                serviceInfo.addCollector(collectorInfo);
            }

            serviceInfoMap.put(serviceName, serviceInfo);
        }
        return serviceInfoMap;
    }

}
