package cloud.benchflow.testmanager.bundle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 14.02.17.
 */
public class TestBundle {

  private static final String VALID_TEST_BUNDLE_FILENAME =
      "src/test/resources/data/wfms.camunda.valid.zip";
  private static final String NO_TRIAL_KEY_TEST_BUNDLE_FILENAME =
      "src/test/resources/data/wfms.camunda.invalid.no.users.key.zip";
  private static final String NO_DEFINITION_TEST_BUNDLE_FILENAME =
      "src/test/resources/data/wfms.camunda.invalid.no.definition.zip";

  public static final int BPMN_MODELS_COUNT = 7;

  public static InputStream getValidTestBundle() throws FileNotFoundException {

    return new FileInputStream(VALID_TEST_BUNDLE_FILENAME);
  }

  public static File getValidTestBundleFile() {
    return new File(VALID_TEST_BUNDLE_FILENAME);
  }

  public static InputStream getNoTrialKeyTestBundle() throws FileNotFoundException {

    return new FileInputStream(NO_TRIAL_KEY_TEST_BUNDLE_FILENAME);
  }

  public static InputStream getNoDefinitionTestBundle() throws FileNotFoundException {
    return new FileInputStream(NO_DEFINITION_TEST_BUNDLE_FILENAME);
  }

  public static ZipInputStream getValidTestBundleZip() throws FileNotFoundException {

    return new ZipInputStream(getValidTestBundle());
  }

  public static ZipInputStream getInValidTestBundleZip() throws FileNotFoundException {

    return new ZipInputStream(getNoTrialKeyTestBundle());
  }

  public static ZipInputStream getNoDefinitionTestBundleZip() throws FileNotFoundException {
    return new ZipInputStream(getNoDefinitionTestBundle());
  }

  public static String getValidTestDefinitionString() throws IOException {
    return BenchFlowTestBundleExtractor
        .extractBenchFlowTestDefinitionString(getValidTestBundleZip());
  }

  public static String getValidDeploymentDescriptorString() throws IOException {
    return BenchFlowTestBundleExtractor.extractDeploymentDescriptorString(getValidTestBundleZip());
  }

  public static InputStream getValidTestDefinitionInputStream() throws IOException {

    String testDefinitionString =
        BenchFlowTestBundleExtractor.extractBenchFlowTestDefinitionString(getValidTestBundleZip());

    if (testDefinitionString == null) {
      return null;
    }

    return new ByteArrayInputStream(testDefinitionString.getBytes());
  }

  public static InputStream getValidDeploymentDescriptorInputStream() throws IOException {

    return BenchFlowTestBundleExtractor
        .extractDeploymentDescriptorInputStream(getValidTestBundleZip());
  }

  public static Map<String, InputStream> getValidBPMNModels() throws IOException {

    return BenchFlowTestBundleExtractor.extractBPMNModelInputStreams(getValidTestBundleZip());
  }
}
