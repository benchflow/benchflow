package cloud.benchflow.testmanager.services.external;

import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * uses Jersey Client: http://www.dropwizard.io/1.0.6/docs/manual/client.html
 * https://jersey.java.net/documentation/2.22.1/client.html
 *
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 18.12.16.
 */
public class BenchFlowExperimentManagerService {

  // TODO - move this to common library?
  private static String RUN_PATH = "/run";
  private static String ABORT_PATH = "/abort";
  public static String STATUS_PATH = "/status";

  private Logger logger =
      LoggerFactory.getLogger(BenchFlowExperimentManagerService.class.getSimpleName());

  private WebTarget experimentManagerTarget;

  public BenchFlowExperimentManagerService(Client httpClient, String experimentManagerAddress) {

    this.experimentManagerTarget = httpClient.target("http://" + experimentManagerAddress);
  }

  public void runBenchFlowExperiment(String experimentID) {

    logger.info("runBenchFlowExperiment: " + experimentID);

    Response runPEResponse =
        experimentManagerTarget.path(BenchFlowConstants.getPathFromExperimentID(experimentID))
            .path(RUN_PATH).request().post(null);

    if (runPEResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {

      // TODO - handle possible errors and throw exceptions accordingly
      logger.error("runBenchFlowExperiment: error connecting - " + runPEResponse.getStatus());

    } else {
      logger.info("runBenchFlowExperiment: connected successfully");
    }
  }

  public void abortBenchFlowExperiment(String experimentID) {

    logger.info("abortBenchFlowExperiment: " + experimentID);

    Response abortExperimentResponse =
        experimentManagerTarget.path(BenchFlowConstants.getPathFromExperimentID(experimentID))
            .path(ABORT_PATH).request().post(null);

    if (abortExperimentResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {

      // TODO - handle possible errors and throw exceptions accordingly

      logger.error(
          "abortBenchFlowExperiment: error connecting - " + abortExperimentResponse.getStatus());

    } else {
      logger.info("abortBenchFlowExperiment: connected successfully");
    }
  }
}
