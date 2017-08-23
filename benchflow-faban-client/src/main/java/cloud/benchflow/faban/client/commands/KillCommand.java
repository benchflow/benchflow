package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.configurations.RunConfig;
import cloud.benchflow.faban.client.exceptions.EmptyHarnessResponseException;
import cloud.benchflow.faban.client.exceptions.FabanClientBadRequestException;
import cloud.benchflow.faban.client.exceptions.IllegalRunStatusException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * Faban Kill Command.
 *
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 29/10/15.
 */
public class KillCommand extends Configurable<RunConfig> implements Command<RunStatus> {

  private static String KILL_URL = "/kill";

  public RunStatus exec(FabanClientConfig fabanConfig)
      throws RunIdNotFoundException, IOException, IllegalRunStatusException,
      EmptyHarnessResponseException, MalformedURIException, FabanClientBadRequestException {
    return kill(fabanConfig);
  }

  private RunStatus kill(FabanClientConfig fabanConfig)
      throws RunIdNotFoundException, IOException, EmptyHarnessResponseException,
      IllegalRunStatusException, MalformedURIException, FabanClientBadRequestException {

    RunId runId = config.getRunId();

    //TODO: evaluate if it possible to convert back to lamba expression handling (line 43)
    //      when checked exceptions are supported. See: http://www.baeldung.com/java-lambda-exceptions
    //    ResponseHandler<RunStatus> rh =
    //        resp -> new RunStatus(new BasicResponseHandler().handleEntity(resp.getEntity()), runId);

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
      if (statusCode == HttpStatus.SC_NOT_FOUND) {
        throw new RunIdNotFoundException("Run id not found");
      } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
        throw new FabanClientBadRequestException("Bad kill request to harness");
      } else if (statusCode == HttpStatus.SC_NO_CONTENT) {
        throw new EmptyHarnessResponseException();
      }

      //Handle generic HTTP exceptions (TODO: determine the expected HTTP status from Faban, and validate we get that one)
      return new RunStatus(new BasicResponseHandler().handleResponse(resp), runId);

    } catch (URISyntaxException e) {
      throw new MalformedURIException("Attempted to kill to malformed URI " + e.getInput(), e);
    }

  }

}
