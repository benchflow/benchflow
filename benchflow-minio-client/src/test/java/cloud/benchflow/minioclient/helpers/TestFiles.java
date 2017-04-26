package cloud.benchflow.minioclient.helpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public class TestFiles {

  private static String VALID_TEST_DEFINITION_FILE_PATH =
      "src/test/resources/data/ParallelMultiple11/benchflow-test.yml";
  private static String VALID_TEST_DEPLOYMENT_DESCRIPTOR_FILE_PATH =
      "src/test/resources/data/ParallelMultiple11/docker-compose.yml";

  private static String VALID_BPMN_MODEL_FILE_PATH =
      "src/test/resources/data/ParallelMultiple11/models/11ParallelStructured.bpmn";
  private static String VALID_BPMN_MODEL_FILE_NAME = "11ParallelStructured.bpmn";
  private static String VALID_MOCK_MODEL_FILE_PATH =
      "src/test/resources/data/ParallelMultiple11/models/mock.bpmn";
  private static String VALID_MOCK_MODEL_FILE_NAME = "mock.bpmn";

  private static String VALID_EXPERIMENT_DEFINITION_FILE_PATH =
      "src/test/resources/data/ParallelMultiple11/1/docker-compose.yml";

  public static InputStream getValidTestDefinitionInputStream() throws FileNotFoundException {
    return new FileInputStream(VALID_TEST_DEFINITION_FILE_PATH);
  }

  public static String getValidTestDefinitionString() throws IOException {
    return IOUtils.toString(getValidTestDefinitionInputStream(), StandardCharsets.UTF_8);
  }

  public static InputStream getValidDeploymentDescriptorInputStream() throws FileNotFoundException {
    return new FileInputStream(VALID_TEST_DEPLOYMENT_DESCRIPTOR_FILE_PATH);
  }

  public static String getValidDeploymentDescriptorString() throws IOException {
    return IOUtils.toString(getValidDeploymentDescriptorInputStream(), StandardCharsets.UTF_8);
  }

  public static Map<String, InputStream> getValidBPMNModels() throws FileNotFoundException {

    Map<String, InputStream> map = new HashMap<>();

    map.put(VALID_BPMN_MODEL_FILE_NAME, new FileInputStream(VALID_BPMN_MODEL_FILE_PATH));
    map.put(VALID_MOCK_MODEL_FILE_NAME, new FileInputStream(VALID_MOCK_MODEL_FILE_PATH));

    return map;
  }

  public static InputStream getValidExperimentDefinitionInputStream() throws FileNotFoundException {
    return new FileInputStream(VALID_EXPERIMENT_DEFINITION_FILE_PATH);
  }

  public static String getValidExperimentDefinitionString() throws IOException {
    return IOUtils.toString(getValidExperimentDefinitionInputStream(), StandardCharsets.UTF_8);
  }
}
