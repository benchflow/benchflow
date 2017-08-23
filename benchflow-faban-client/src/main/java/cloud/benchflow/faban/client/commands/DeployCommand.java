package cloud.benchflow.faban.client.commands;

import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.DeployConfig;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.exceptions.DeployException;
import cloud.benchflow.faban.client.exceptions.FabanClientBadRequestException;
import cloud.benchflow.faban.client.exceptions.MalformedURIException;
import cloud.benchflow.faban.client.responses.DeployStatus;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


/**
 * Faban Deploy Command.
 *
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 26/10/15.
 */
public class DeployCommand extends Configurable<DeployConfig> implements Command<DeployStatus> {

  private static String DEPLOY_URL = "/deploy";

  /**
   * Deploy a Faban benchmark.
   *
   * @param fabanConfig the harness configuration
   * @return a response containing the status of the operation
   * @throws IOException when there are issues in reading the benchmark file
   */
  public DeployStatus exec(FabanClientConfig fabanConfig)
      throws IOException, DeployException, MalformedURIException, FabanClientBadRequestException {
    return deploy(fabanConfig);
  }


  /*  Since deploying a stream to Faban suddenly doesn’t work anymore, we
      changed the API to deploy a File (which works as expected).
  
      A stream would have been better because it wouldn’t have required to
      have a file on the file system, reducing the IO of the components using
      the library.
      Nevertheless, this solution is good enough until we figure out how to
      send a stream of data to Faban. */
  private DeployStatus deploy(FabanClientConfig fabanConfig)
      throws IOException, DeployException, MalformedURIException, FabanClientBadRequestException {

    //TODO: evaluate if it possible to convert back to lamba expression handling (line 43)
    //      when checked exceptions are supported. See: http://www.baeldung.com/java-lambda-exceptions
    //    ResponseHandler<DeployStatus> dh = resp -> {
    //      System.out.println(resp);
    //      return new DeployStatus(resp.getStatusLine().getStatusCode());
    //    };

    File jarFile = this.config.getJarFile();
    Boolean clearConfig = config.clearConfig();

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

      URI deployURL = new URIBuilder(fabanConfig.getMasterURL()).setPath(DEPLOY_URL).build();
      HttpPost post = new HttpPost(deployURL);

      HttpEntity multipartEntity = MultipartEntityBuilder.create()
          .addTextBody("user", fabanConfig.getUser())
          .addTextBody("password", fabanConfig.getPassword()).addBinaryBody("jarfile", jarFile,
              ContentType.create("application/java-archive"), config.getDriverName())
          .addTextBody("clearconfig", clearConfig.toString()).build();

      post.setEntity(multipartEntity);
      post.setHeader("Accept", "text/plain");

      CloseableHttpResponse resp = httpclient.execute(post);
      int status = resp.getStatusLine().getStatusCode();

      if (status == HttpStatus.SC_BAD_REQUEST) {
        throw new FabanClientBadRequestException("Bad request");
      }

      DeployStatus dresp = new DeployStatus(resp.getStatusLine().getStatusCode());

      return dresp;

    } catch (URISyntaxException e) { //this should never occur
      throw new MalformedURIException("Attempted to deploy to malformed URI: " + e.getInput(), e);
    }

  }

}
