package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.configurations.SubmitConfig;
import cloud.benchflow.faban.client.exceptions.BenchmarkNameNotFoundException;
import cloud.benchflow.faban.client.exceptions.EmptyHarnessResponseException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.responses.RunId;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 28/10/15.
 */
public class SubmitCommand extends Configurable<SubmitConfig> implements Command<RunId> {

  private static String SUBMIT_URL = "/submit";

  public RunId exec(FabanClientConfig fabanConfig)
      throws IOException, BenchmarkNameNotFoundException {
    return submit(fabanConfig);
  }

  /**
   * Run a Faban benchmark.
   * @param fabanConfig the harness configuration
   * @return a response containing the status of the operation
   * @throws IOException when there are issues in reading the benchmark file
   * @throws BenchmarkNameNotFoundException when the requested benchmark is not found
   */
  public RunId submit(FabanClientConfig fabanConfig)
      throws IOException, BenchmarkNameNotFoundException {


    String benchmarkName = config.getBenchmarkName();
    String profile = config.getProfile();
    InputStream configFile = config.getConfigFile();

    ResponseHandler<RunId> sh =
        resp -> new RunId(new BasicResponseHandler().handleEntity(resp.getEntity()));

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      URI submitURL = new URIBuilder(fabanConfig.getMasterURL())
          .setPath(SUBMIT_URL + "/" + benchmarkName + "/" + profile).build();

      HttpPost post = new HttpPost(submitURL);
      HttpEntity multipartEntity = MultipartEntityBuilder.create()
          .addTextBody("sun", fabanConfig.getUser()).addTextBody("sp", fabanConfig.getPassword())
          .addBinaryBody("configfile", ByteStreams.toByteArray(configFile),
              //ContentType.DEFAULT_BINARY,
              ContentType.create("application/octet-stream"), "run.xml")
          .build();

      post.setEntity(multipartEntity);

      CloseableHttpResponse resp = httpClient.execute(post);
      int statusCode = resp.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_NOT_FOUND) {
        throw new BenchmarkNameNotFoundException("Benchmark " + benchmarkName + " not deployed.");
      } else if (statusCode == HttpStatus.SC_NO_CONTENT) {
        throw new EmptyHarnessResponseException();
      }

      //TODO: check that this does indeed work
      RunId runId = sh.handleResponse(resp);

      return runId;

    } catch (URISyntaxException e) {
      throw new MalformedURIException("Attempted to submit run for benchmark " + benchmarkName
          + "and profile " + profile + "to malformed URL.");
    }

  }

}
