package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.configurations.ShowLogsConfig;
import cloud.benchflow.faban.client.exceptions.EmptyHarnessResponseException;
import cloud.benchflow.faban.client.exceptions.FabanClientBadRequestException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.RunLogStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Faban ShowLogs Command.
 *
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 11/11/15.
 */
public class ShowLogsCommand extends Configurable<ShowLogsConfig> implements Command<RunLogStream> {

  private static String SHOWLOGS_URL = "/logs";


  public RunLogStream exec(FabanClientConfig fabanConfig)
      throws IOException, RunIdNotFoundException, EmptyHarnessResponseException,
      MalformedURIException, FabanClientBadRequestException {
    return showlogs(fabanConfig);
  }

  private RunLogStream showlogs(FabanClientConfig fabanConfig)
      throws IOException, RunIdNotFoundException, EmptyHarnessResponseException,
      MalformedURIException, FabanClientBadRequestException {

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      URI pendingURL = new URIBuilder(fabanConfig.getMasterURL())
          .setPath(SHOWLOGS_URL + "/" + config.getRunId()).build();

      HttpGet get = new HttpGet(pendingURL);
      CloseableHttpResponse resp = httpClient.execute(get);
      int status = resp.getStatusLine().getStatusCode();

      if (status == HttpStatus.SC_NO_CONTENT) {
        throw new EmptyHarnessResponseException(
            "Harness returned empty response to pending request");
      } else if (status == HttpStatus.SC_BAD_REQUEST) {
        throw new FabanClientBadRequestException("Bad request");
      } else if (status == HttpStatus.SC_NOT_FOUND) {
        throw new RunIdNotFoundException("Run id not found");
      }

      HttpEntity ent = resp.getEntity();
      InputStream in = resp.getEntity().getContent();
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(in, ent.getContentEncoding().getValue()));
      return new RunLogStream(reader);

    } catch (URISyntaxException e) {
      throw new MalformedURIException(
          "Malformed showlogs request to faban harness: " + e.getInput(), e);
    }

  }

}
