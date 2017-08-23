package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.configurations.RunConfig;
import cloud.benchflow.faban.client.exceptions.FabanClientBadRequestException;
import cloud.benchflow.faban.client.exceptions.IllegalRunInfoResultException;
import cloud.benchflow.faban.client.exceptions.IllegalRunStatusException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunInfo;
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
import org.jsoup.Jsoup;

/**
 * Faban RunInfo Command.
 *
 * @author vincenzoferme
 */
public class RunInfoCommand extends Configurable<RunConfig> implements Command<RunInfo> {

  private static String RUNINFO_PATH = "/results/get_run_info";
  private static String RUNINFO_RUNID_PAR = "runId";

  public RunInfo exec(FabanClientConfig fabanConfig)
      throws IOException, RunIdNotFoundException, IllegalRunStatusException,
      IllegalRunInfoResultException, FabanClientBadRequestException, MalformedURIException {
    return runInfo(fabanConfig);
  }

  private RunInfo runInfo(FabanClientConfig fabanConfig)
      throws IOException, RunIdNotFoundException, IllegalRunStatusException,
      IllegalRunInfoResultException, FabanClientBadRequestException, MalformedURIException {

    RunId runId = config.getRunId();

    //TODO: evaluate if it possible to convert back to lamba expression handling (line 43)
    //      when checked exceptions are supported. See: http://www.baeldung.com/java-lambda-exceptions
    //    ResponseHandler<RunInfo> sh =
    //        resp -> new RunInfo(Jsoup.parse(new BasicResponseHandler().handleEntity(resp.getEntity())),
    //            runId);

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

      URI statusURL = new URIBuilder(fabanConfig.getControllerURL() + RUNINFO_PATH)
          .addParameter(RUNINFO_RUNID_PAR, runId.toString()).build();

      HttpGet get = new HttpGet(statusURL);

      CloseableHttpResponse resp = httpclient.execute(get);

      int status = resp.getStatusLine().getStatusCode();
      if (status == HttpStatus.SC_NOT_FOUND) {
        throw new RunIdNotFoundException("Run id not found");
      } else if (status == HttpStatus.SC_BAD_REQUEST) {
        throw new FabanClientBadRequestException("Illegal runInfo request");
      }

      //Handle generic HTTP exceptions (TODO: determine the expected HTTP status from Faban, and validate we get that one)
      //TODO: check that the call to .handleResponse(..) actually returns the expected string
      RunInfo runInfo =
          new RunInfo(Jsoup.parse(new BasicResponseHandler().handleResponse(resp)), runId);

      return runInfo;

    } catch (URISyntaxException e) {
      throw new MalformedURIException(
          "Attempted to check runInfo from malformed URI: " + e.getInput(), e);
    }

  }


}
