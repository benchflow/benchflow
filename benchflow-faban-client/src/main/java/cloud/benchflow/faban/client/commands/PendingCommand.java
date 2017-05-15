package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.exceptions.EmptyHarnessResponseException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunQueue;

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
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 30/10/15.
 */
public class PendingCommand extends Configurable implements Command<RunQueue> {

  private static String PENDING_URL = "/pending";

  public RunQueue exec(FabanClientConfig fabanConfig) throws IOException {
    return pending(fabanConfig);
  }

  private RunQueue pending(FabanClientConfig fabanConfig) throws IOException {

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      URI pendingURL = new URIBuilder(fabanConfig.getMasterURL()).setPath(PENDING_URL).build();

      RunQueue queue = new RunQueue();

      HttpGet get = new HttpGet(pendingURL);
      CloseableHttpResponse resp = httpClient.execute(get);
      int status = resp.getStatusLine().getStatusCode();

      if (status == HttpStatus.SC_NO_CONTENT) {
        throw new EmptyHarnessResponseException(
            "Harness returned empty response to pending request");
      }

      HttpEntity ent = resp.getEntity();
      InputStream in = resp.getEntity().getContent();

      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(in, ent.getContentEncoding().getValue()))) {

        String line;
        while ((line = reader.readLine()) != null) {
          queue.add(new RunId(line));
        }

      }

      return queue;

    } catch (URISyntaxException e) {
      throw new MalformedURIException("Malformed pending request to faban harness: " + e.getInput(),
          e);
    }

  }

}
