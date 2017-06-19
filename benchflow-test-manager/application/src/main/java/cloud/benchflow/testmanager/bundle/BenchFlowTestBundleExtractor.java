package cloud.benchflow.testmanager.bundle;

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.BPMN_MODELS_FOLDER_NAME;
import static cloud.benchflow.testmanager.constants.BenchFlowConstants.DEPLOYMENT_DESCRIPTOR_NAME;
import static cloud.benchflow.testmanager.constants.BenchFlowConstants.TEST_EXPERIMENT_DEFINITION_NAME;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 15.02.17.
 */
public class BenchFlowTestBundleExtractor {

  //    benchflow-test.yaml or .yml
  //    docker-compose.yaml or .yml
  //    optionally the model folder (only for WfMSs)

  private static BiPredicate<ZipEntry, String> entryMatches =
      (zipEntry, regEx) -> zipEntry.getName().matches(regEx);
  private static String definitionRegEx = getYamlRegExFromName(TEST_EXPERIMENT_DEFINITION_NAME);
  private static Predicate<ZipEntry> isExpConfig =
      entry -> entryMatches.test(entry, definitionRegEx);
  private static String deploymentDescriptorRegEx =
      getYamlRegExFromName(DEPLOYMENT_DESCRIPTOR_NAME);
  private static Predicate<ZipEntry> isDeploymentDescriptor =
      entry -> entryMatches.test(entry, deploymentDescriptorRegEx);

  /**
   * Extract test definition as a string.
   *
   * @param benchFlowTestBundle test bundle zip
   * @return test definition String
   * @throws IOException if test definition cannot be found
   */
  public static String extractBenchFlowTestDefinitionString(ZipInputStream benchFlowTestBundle)
      throws IOException {

    return (String) extractBenchFlowTestDefinitionObject(benchFlowTestBundle, ReturnType.STRING,
        isExpConfig);
  }

  /**
   * Extract test definition as an input stream.
   *
   * @param benchFlowTestBundle test bundle zip
   * @return test definition InputStream
   * @throws IOException if test definition cannot be found
   */
  public static InputStream extractBenchFlowTestDefinitionInputStream(
      ZipInputStream benchFlowTestBundle) throws IOException {

    return (InputStream) extractBenchFlowTestDefinitionObject(benchFlowTestBundle,
        ReturnType.INPUT_STREAM, isExpConfig);
  }

  /**
   * Extract deployment descriptor as an input stream.
   *
   * @param benchFlowTestBundle test bundle zip
   * @return deployment descriptor InputStream
   * @throws IOException if deployment descriptor cannot be found
   */
  public static InputStream extractDeploymentDescriptorInputStream(
      ZipInputStream benchFlowTestBundle) throws IOException {

    return (InputStream) extractBenchFlowTestDefinitionObject(benchFlowTestBundle,
        ReturnType.INPUT_STREAM, isDeploymentDescriptor);
  }

  /**
   * Extract deployment descriptor as a string.
   *
   * @param benchFlowTestBundle test bundle zip
   * @return deployment descriptor String
   * @throws IOException if deployment descriptor cannot be found
   */
  public static String extractDeploymentDescriptorString(ZipInputStream benchFlowTestBundle)
      throws IOException {

    return (String) extractBenchFlowTestDefinitionObject(benchFlowTestBundle, ReturnType.STRING,
        isDeploymentDescriptor);
  }

  private static Object extractBenchFlowTestDefinitionObject(ZipInputStream benchFlowTestBundle,
      ReturnType returnType, Predicate<ZipEntry> isFile) throws IOException {

    ZipEntry zipEntry;

    while ((zipEntry = benchFlowTestBundle.getNextEntry()) != null) {

      if (isFile.test(zipEntry)) {

        switch (returnType) {
          case INPUT_STREAM:
            return readZipEntryToInputStream(benchFlowTestBundle);
          case STRING:
            return readZipEntryToString(benchFlowTestBundle);
          default:
            return null;
        }
      }
    }

    return null;
  }

  /**
   * Extract BPMN models.
   *
   * @param benchFlowTestBundle test bundle zip
   * @return Map with BPMN model filename and InputStream
   * @throws IOException if file cannot be found
   */
  public static Map<String, InputStream> extractBPMNModelInputStreams(
      ZipInputStream benchFlowTestBundle) throws IOException {

    // TODO - validate that the names are the same as in the test definition

    BiPredicate<ZipEntry, String> isBPMNModelEntry = (zipEntry,
        string) -> zipEntry.getName().contains(string) && !zipEntry.getName().contains("._");

    Predicate<ZipEntry> isBPMNModel =
        entry -> isBPMNModelEntry.test(entry, BPMN_MODELS_FOLDER_NAME + "/");

    Map<String, InputStream> models = new HashMap<>();

    ZipEntry zipEntry;

    while ((zipEntry = benchFlowTestBundle.getNextEntry()) != null) {

      if (!zipEntry.isDirectory() && isBPMNModel.test(zipEntry)) {

        String fileName = zipEntry.getName().substring(zipEntry.getName().lastIndexOf("/") + 1);

        InputStream data = readZipEntryToInputStream(benchFlowTestBundle);

        models.put(fileName, data);
      }
    }

    return models;
  }

  private static String getYamlRegExFromName(String name) {

    return "^(?:.*\\/)?(" + name + "\\.(yml|yaml))$";
  }

  private static String readZipEntryToString(ZipInputStream inputStream) throws IOException {

    return readZipEntryToOutputStream(inputStream).toString(StandardCharsets.UTF_8.name());
  }

  private static InputStream readZipEntryToInputStream(ZipInputStream inputStream)
      throws IOException {

    ByteArrayInputStream resultInputStream =
        new ByteArrayInputStream(readZipEntryToOutputStream(inputStream).toByteArray());

    return resultInputStream;
  }

  private static ByteArrayOutputStream readZipEntryToOutputStream(ZipInputStream inputStream)
      throws IOException {

    byte[] buffer = new byte[1024];

    int len;

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    while ((len = inputStream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }

    return out;
  }

  private enum ReturnType {
    STRING, INPUT_STREAM
  }
}
