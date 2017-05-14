package cloud.benchflow.testmanager.archive;

import java.io.*;
import java.util.Map;
import java.util.zip.ZipInputStream;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 14.02.17. */
public class TestArchives {

  private static final String VALID_TEST_ARCHIVE_FILENAME =
      "src/test/resources/data/wfms.camunda.valid.zip";
  private static final String NO_TRIAL_KEY_TEST_ARCHIVE_FILENAME =
      "src/test/resources/data/wfms.camunda.invalid.no.users.key.zip";
  private static final String NO_DEFINITION_TEST_ARCHVIE_FILENAME =
      "src/test/resources/data/wfms.camunda.invalid.no.definition.zip";

  public static final int BPMN_MODELS_COUNT = 7;

  public static InputStream getValidTestArchive() throws FileNotFoundException {

    return new FileInputStream(VALID_TEST_ARCHIVE_FILENAME);
  }

  public static File getValidTestArchiveFile() {
    return new File(VALID_TEST_ARCHIVE_FILENAME);
  }

  public static InputStream getNoTrialKeyTestArchive() throws FileNotFoundException {

    return new FileInputStream(NO_TRIAL_KEY_TEST_ARCHIVE_FILENAME);
  }

  public static InputStream getNoDefinitionTestArchive() throws FileNotFoundException {
    return new FileInputStream(NO_DEFINITION_TEST_ARCHVIE_FILENAME);
  }

  public static ZipInputStream getValidTestArchiveZip() throws FileNotFoundException {

    return new ZipInputStream(getValidTestArchive());
  }

  public static ZipInputStream getInValidTestArchiveZip() throws FileNotFoundException {

    return new ZipInputStream(getNoTrialKeyTestArchive());
  }

  public static ZipInputStream getNoDefinitionTestArchiveZip() throws FileNotFoundException {
    return new ZipInputStream(getNoDefinitionTestArchive());
  }

  public static String getValidTestDefinitionString() throws IOException {
    return BenchFlowTestArchiveExtractor
        .extractBenchFlowTestDefinitionString(getValidTestArchiveZip());
  }

  public static String getValidDeploymentDescriptorString() throws IOException {
    return BenchFlowTestArchiveExtractor
        .extractDeploymentDescriptorString(getValidTestArchiveZip());
  }

  public static InputStream getValidTestDefinitionInputStream() throws IOException {

    String testDefinitionString = BenchFlowTestArchiveExtractor
        .extractBenchFlowTestDefinitionString(getValidTestArchiveZip());

    if (testDefinitionString == null)
      return null;

    return new ByteArrayInputStream(testDefinitionString.getBytes());
  }

  public static InputStream getValidDeploymentDescriptorInputStream() throws IOException {

    return BenchFlowTestArchiveExtractor
        .extractDeploymentDescriptorInputStream(getValidTestArchiveZip());
  }

  public static Map<String, InputStream> getValidBPMNModels() throws IOException {

    return BenchFlowTestArchiveExtractor.extractBPMNModelInputStreams(getValidTestArchiveZip());
  }
}
