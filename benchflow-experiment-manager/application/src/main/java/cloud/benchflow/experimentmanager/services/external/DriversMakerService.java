package cloud.benchflow.experimentmanager.services.external;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

import cloud.benchflow.experimentmanager.exceptions.web.BenchmarkGenerationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 05.03.17.
 */
public class DriversMakerService {

  public static final String GENERATE_BENCHMARK_PATH = "/generatedriver";

  private Logger logger = LoggerFactory.getLogger(DriversMakerService.class.getSimpleName());

  private WebTarget driversMakerTarget;

  private int numConnectionRetries;

  public DriversMakerService(Client httpClient, String driversMakerAddress, int numConnectionRetries) {

    this.driversMakerTarget = httpClient.target("http://" + driversMakerAddress);
    this.numConnectionRetries = numConnectionRetries;
  }

  public void generateBenchmark(String experimentName, long experimentNumber, int numTrials) {

    logger.info("generateBenchmark for " + experimentName + MODEL_ID_DELIMITER + experimentNumber
        + " with " + numTrials + " trials.");

    // TODO - return result as a list of IDs

    MakeDriverRequestBody body = new MakeDriverRequestBody();
    body.setExperimentName(experimentName);
    body.setExperimentNumber(experimentNumber);
    body.setTrials(numTrials);

    Response response = driversMakerTarget.path(GENERATE_BENCHMARK_PATH).request()
        .post(Entity.entity(body, MediaType.APPLICATION_JSON));

    if (response.getStatus() != Response.Status.OK.getStatusCode()) {

      logger.error("generateBenchmark: error connecting - " + response.getStatus());
      throw new BenchmarkGenerationException("Error in benchmark generation", response.getStatus());
    }

    logger.info("generateBenchmark: generated Benchmark with response: "
        + response.readEntity(String.class));
  }

  private static class MakeDriverRequestBody {

    private String experimentName;
    private long experimentNumber;
    private int trials;

    MakeDriverRequestBody() {}

    public String getExperimentName() {
      return experimentName;
    }

    public void setExperimentName(String experimentName) {
      this.experimentName = experimentName;
    }

    public long getExperimentNumber() {
      return experimentNumber;
    }

    public void setExperimentNumber(long experimentNumber) {
      this.experimentNumber = experimentNumber;
    }

    public int getTrials() {
      return trials;
    }

    public void setTrials(int trials) {
      this.trials = trials;
    }
  }
}
