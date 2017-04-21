package cloud.benchflow.plugins.wfms.activiti;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private Map<String, String> JSONHeaders;
    private Map<String, String> AuthorizationHeaders;
    private Map<String, String> AllHeaders;
    protected String processDefinitionAPI;
    private JsonParser parser;


    public WfMSPlugin(String sutEndpoint) {
        super(sutEndpoint,"/service/repository/deployments");
        processDefinitionAPI = sutEndpoint + "/service/repository/process-definitions";
        parser = new JsonParser();
        //this.modelsStartID = new HashMap<String, String>();
        JSONHeaders = new TreeMap<String, String>();
        JSONHeaders.put("Content-Type","application/json");

        AuthorizationHeaders = new TreeMap<String, String>();
        // Adds authorization details for the default kermit user (admin)
        AuthorizationHeaders.put("Authorization","Basic a2VybWl0Omtlcm1pdA==");

        AllHeaders = new TreeMap<String, String>();
        AllHeaders.putAll(JSONHeaders);
        AllHeaders.putAll(AuthorizationHeaders);
    }

    @Override
    public Map<String, String> deploy(File model) throws IOException {

        Map<String, String> result = new HashMap<String, String>();
        StringPart deploymentName = new StringPart("deployment-name", model.getName());
        List<Part> parts = new ArrayList<Part>();

        FilePart process = new FilePart("*", model);

        parts.add(deploymentName);
        parts.add(process);
        StringBuilder deployDef = http.fetchURL(deployAPI, parts, AuthorizationHeaders);

        JsonObject deployObj = parser.parse(deployDef.toString()).getAsJsonObject();
        String deploymentId = deployObj.get("id").getAsString();

        //Obtain process definition data
        StringBuilder procDef = http.fetchURL(processDefinitionAPI + "?deploymentId=" + deploymentId, AuthorizationHeaders);
        String processDefinitionResponse = procDef.toString();

        JsonObject procDefArray = parser.parse(processDefinitionResponse).getAsJsonObject();
        //We only get 1 element using the deploymentId
        String processDefinitionId = procDefArray.get("data").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
        result.put(model.getName(), processDefinitionId);
        return result;

    }

    public String startProcessInstance(String processDefinitionId, String data) throws IOException {
        String startURL = sutEndpoint + "/service/runtime/process-instances";

        JsonObject body = new JsonObject();
        body.addProperty("processDefinitionId", modelsStartID.get(processDefinitionId));

        // create the gson using the GsonBuilder. Set pretty printing on. Allow
        // serializing null and set all fields to the Upper Camel Case
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

        StringBuilder responseStart = http.fetchURL(startURL, gson.toJson(body), AllHeaders);
        return responseStart.toString();
    }
}
