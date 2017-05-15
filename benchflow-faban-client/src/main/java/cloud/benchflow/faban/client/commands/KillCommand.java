package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.configurations.StatusConfig;
import cloud.benchflow.faban.client.exceptions.EmptyHarnessResponseException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Simone D'Avico <simonedavico@gmail.com>
 *
 * Created on 29/10/15.
 */
public class KillCommand extends Configurable<StatusConfig> implements Command<RunStatus> {

  private static String KILL_URL = "/kill";

  public RunStatus exec(FabanClientConfig fabanConfig) throws RunIdNotFoundException, IOException {
    return kill(fabanConfig);
  }

  private RunStatus kill(FabanClientConfig fabanConfig) throws RunIdNotFoundException, IOException {

    RunId runId = config.getRunId();

    ResponseHandler<RunStatus> rh =
        resp -> new RunStatus(new BasicResponseHandler().handleEntity(resp.getEntity()), runId);

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      URI killURL =
          new URIBuilder(fabanConfig.getMasterURL()).setPath(KILL_URL + "/" + runId).build();

      //TODO: check that this setup creates the correct request
      List<NameValuePair> params = new ArrayList<>();
      params.add(new BasicNameValuePair("sun", fabanConfig.getUser()));
      params.add(new BasicNameValuePair("sp", fabanConfig.getPassword()));

      HttpPost post = new HttpPost(killURL);
      post.setEntity(new UrlEncodedFormEntity(params));

      CloseableHttpResponse resp = httpClient.execute(post);

      int statusCode = resp.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_NOT_FOUND)
        throw new RunIdNotFoundException();
      if (statusCode == HttpStatus.SC_BAD_REQUEST)
        throw new FabanClientException("Bad kill request to harness");
      if (statusCode == HttpStatus.SC_NO_CONTENT)
        throw new EmptyHarnessResponseException();

      return rh.handleResponse(resp);

    } catch (URISyntaxException e) {
      throw new MalformedURIException("Attempted to kill to malformed URI " + e.getInput(), e);
    }

  }

}
