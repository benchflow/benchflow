package cloud.benchflow.libraries.wfms;

import java.util.Map;
import java.util.logging.*;

import java.io.File;
import java.io.IOException;

public abstract class WfMSApi {

    protected String sutEndpoint;
    protected String deployAPI;
    protected Logger logger;

    public WfMSApi(String se, String d) {
        sutEndpoint = se;
        deployAPI = sutEndpoint + d;
        logger = Logger.getLogger("WfMSApi");
        logger.info("[WfMSApi] Deploy api: " + deployAPI);
    }

    public abstract Map<String, String> deploy(File model) throws IOException;

    public abstract String startProcessInstance(String processDefinitionId, String data) throws IOException;

}