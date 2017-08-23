package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.configurations.RunConfig;
import cloud.benchflow.faban.client.exceptions.FabanClientBadRequestException;
import cloud.benchflow.faban.client.exceptions.IllegalRunStatusException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Faban Status Command.
 *
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 28/10/15.
 */
public class StatusCommand extends Configurable<RunConfig> implements Command<RunStatus> {

  private static String STATUS_PATH = "/status";

  public RunStatus exec(FabanClientConfig fabanConfig) throws IOException, RunIdNotFoundException,
      IllegalRunStatusException, MalformedURIException, FabanClientBadRequestException {
    return status(fabanConfig);
  }

  private RunStatus status(FabanClientConfig fabanConfig)
      throws IOException, RunIdNotFoundException, IllegalRunStatusException, MalformedURIException,
      FabanClientBadRequestException {

    RunId runId = config.getRunId();

    //TODO: evaluate if it possible to convert back to lamba expression handling (line 43)
    //      when checked exceptions are supported. See: http://www.baeldung.com/java-lambda-exceptions
    //    ResponseHandler<RunStatus> sh =
    //        resp -> new RunStatus(new BasicResponseHandler().handleEntity(resp.getEntity()), runId);

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

      URI statusURL =
          new URIBuilder(fabanConfig.getMasterURL()).setPath(STATUS_PATH + "/" + runId).build();

      HttpGet get = new HttpGet(statusURL);

      CloseableHttpResponse resp = httpclient.execute(get);

      int status = resp.getStatusLine().getStatusCode();
      if (status == HttpStatus.SC_NOT_FOUND) {
        throw new RunIdNotFoundException("Run id not found");
      } else if (status == HttpStatus.SC_BAD_REQUEST) {
        throw new FabanClientBadRequestException("Illegal status request");
      }

      //Handle generic HTTP exceptions (TODO: determine the expected HTTP status from Faban, and validate we get that one)
      //TODO: check that the call to .handleResponse(..) actually returns the expected string
      RunStatus runStatus = new RunStatus(new BasicResponseHandler().handleResponse(resp), runId);

      return runStatus;

    } catch (URISyntaxException e) {
      throw new MalformedURIException(
          "Attempted to check status from malformed URI: " + e.getInput(), e);
    }

  }


}
