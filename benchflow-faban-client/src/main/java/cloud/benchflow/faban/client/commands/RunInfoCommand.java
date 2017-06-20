package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.configurations.RunConfig;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;

/**
 * @author vincenzoferme
 */
public class RunInfoCommand extends Configurable<RunConfig> implements Command<RunInfo> {

  private static String RUNINFO_PATH = "/results/get_run_info";
  private static String RUNINFO_RUNID_PAR = "runId";

  public RunInfo exec(FabanClientConfig fabanConfig) throws IOException, RunIdNotFoundException {
    return runInfo(fabanConfig);
  }

  private RunInfo runInfo(FabanClientConfig fabanConfig)
      throws IOException, RunIdNotFoundException {

    RunId runId = config.getRunId();

    ResponseHandler<RunInfo> sh =
        resp -> new RunInfo(Jsoup.parse(new BasicResponseHandler().handleEntity(resp.getEntity())), runId);

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

      URI statusURL =
          new URIBuilder(fabanConfig.getControllerURL() + RUNINFO_PATH).addParameter(RUNINFO_RUNID_PAR, runId.toString()).build();

      HttpGet get = new HttpGet(statusURL);

      CloseableHttpResponse resp = httpclient.execute(get);

      int status = resp.getStatusLine().getStatusCode();
      if (status == HttpStatus.SC_NOT_FOUND) {
        throw new RunIdNotFoundException();
      } else if (status == HttpStatus.SC_BAD_REQUEST) {
        throw new FabanClientException("Illegal runInfo request");
      }

      //TODO: check that the call to .handleEntity(..) actually returns the expected string
      RunInfo runInfo = sh.handleResponse(resp);

      return runInfo;

    } catch (URISyntaxException e) {
      throw new MalformedURIException(
          "Attempted to check runInfo from malformed URI: " + e.getInput(), e);
    }

  }


}
