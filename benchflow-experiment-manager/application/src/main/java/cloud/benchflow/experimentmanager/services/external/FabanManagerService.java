package cloud.benchflow.experimentmanager.services.external;

import static cloud.benchflow.experimentmanager.constants.BenchFlowConstants.MODEL_ID_DELIMITER;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-05-21
 */
public class FabanManagerService {

  private static final String TEMP_DIR = "./tmp";
  private static final String BENCHMARK_FILE_ENDING = ".jar";

  private static Logger logger = LoggerFactory.getLogger(FabanManagerService.class.getSimpleName());

  private FabanClient fabanClient;
  private MinioService minioService;

  public FabanManagerService(FabanClient fabanClient) {
    this.fabanClient = fabanClient;
    this.minioService = BenchFlowExperimentManagerApplication.getMinioService();
  }

  // used for testing
  public FabanManagerService(FabanClient fabanClient, MinioService minioService) {
    this.fabanClient = fabanClient;
    this.minioService = minioService;
  }

  public void deployExperimentToFaban(String experimentID, String driversMakerExperimentID,
      long experimentNumber) throws IOException, JarFileNotFoundException {

    logger.info("deploying benchmark to Faban: " + experimentID);

    String fabanExperimentId = getFabanCompatibleExperimentID(experimentID);

    // DEPLOY TO FABAN
    // get the generated benchflow-benchmark.jar from minioService and save to disk so that it can be sent
    InputStream fabanBenchmark =
        minioService.getDriversMakerGeneratedBenchmark(driversMakerExperimentID, experimentNumber);


    // store on disk because there are issues sending InputStream directly
    java.nio.file.Path benchmarkPath = Paths.get(TEMP_DIR).resolve(experimentID)
        .resolve(fabanExperimentId + BENCHMARK_FILE_ENDING);

    FileUtils.copyInputStreamToFile(fabanBenchmark, benchmarkPath.toFile());

    // deploy experiment to Faban
    fabanClient.deploy(benchmarkPath.toFile());

    // remove file that was sent to fabanClient
    FileUtils.forceDelete(benchmarkPath.toFile());

  }

  private String getFabanCompatibleExperimentID(String experimentID) {
    return experimentID.replace(MODEL_ID_DELIMITER, BenchFlowConstants.FABAN_ID_DELIMITER);
  }


}
