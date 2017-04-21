package cloud.benchflow.plugins.wfms.camunda;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cloud.benchflow.libraries.WfMSApi;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.io.File;
import java.io.IOException;

private class WfMSPlugin extends WfMSApi {

    //THIS DOESN'T GO HERE
//    private Map<String,String> modelsStartID;
    private Map<String, String> JSONHeaders;
    protected String processDefinitionAPI;
    private JsonParser parser;


    public WfMSPlugin(String sutEndpoint) {
        super(sutEndpoint, "/deployment/create");
        processDefinitionAPI = sutEndpoint + "/process-definition";
        logger.info("Process definition api: " + processDefinitionAPI);
        parser = new JsonParser();
        //this.modelsStartID = new HashMap<String, String>();
        JSONHeaders = new TreeMap<String, String>();
        JSONHeaders.put("Content-Type","application/json");
    }

    @Override
    public Map<String, String> deploy(File model) throws IOException {

        Map<String, String> result = new HashMap<String, String>();
        StringPart deploymentName = new StringPart("deployment-name", model.getName());
        List<Part> parts = new ArrayList<Part>();

        FilePart process = new FilePart("*", model);

        parts.add(deploymentName);
        parts.add(process);
        StringBuilder deployDef = http.fetchURL(deployAPI, parts);

        JsonObject deployObj = parser.parse(deployDef.toString()).getAsJsonObject();
        String deploymentId = deployObj.get("id").getAsString();

        //Obtain process definition data
        StringBuilder procDef = http.fetchURL(processDefinitionAPI + "?deploymentId=" + deploymentId);
        String processDefinitionResponse = procDef.toString();

        JsonArray procDefArray = parser.parse(processDefinitionResponse).getAsJsonArray();
        //We only get 1 element using the deploymentId
        String processDefinitionId = procDefArray.get(0).getAsJsonObject().get("id").getAsString();
        result.put(model.getName(), processDefinitionId);
        return result;

    }

    public String startProcessInstance(String processDefinitionId, String data) throws IOException {
        String startURL = sutEndpoint + "/process-definition/" + modelsStartID.get(processDefinitionId) + "/start";
        StringBuilder responseStart = http.fetchURL(startURL, "{}", JSONHeaders);
        return responseStart.toString();
    }
}