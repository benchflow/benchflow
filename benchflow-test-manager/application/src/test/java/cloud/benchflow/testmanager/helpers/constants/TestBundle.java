package cloud.benchflow.testmanager.helpers.constants;

import cloud.benchflow.testmanager.constants.BenchFlowConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 14.02.17.
 * @author vincenzoferme
 */
public class TestBundle {

  public static final int BPMN_MODELS_COUNT = 2;

  /**
   * Get a valid test bundle as a File
   *
   * @param temporaryFolder where to create the zip file
   * @return the File with the test bundle
   * @throws IOException if zip file could not be created
   */
  public static File getLoadTestBundleFile(TemporaryFolder temporaryFolder) throws IOException {

    FileSource testDefinitionFileSource =
        new FileSource(TestFiles.getTestLoadFile().getName(), TestFiles.getTestLoadFile());

    return createBundleFile(testDefinitionFileSource, temporaryFolder);
  }

  /**
   *
   * @param temporaryFolder
   * @return
   * @throws IOException
   */
  public static File getTestExplorationOneAtATimeUsersBundleFile(TemporaryFolder temporaryFolder)
      throws IOException {

    FileSource testDefinitionFileSource =
        new FileSource(TestFiles.getTestExplorationOneAtATimeUsersFile().getName(),
            TestFiles.getTestExplorationOneAtATimeUsersFile());

    return createBundleFile(testDefinitionFileSource, temporaryFolder);

  }

  public static File getTestExplorationRandomUsersBundleFile(TemporaryFolder temporaryFolder)
      throws IOException {

    FileSource testDefinitionFileSource =
        new FileSource(TestFiles.getTestExplorationRandomUsersFile().getName(),
            TestFiles.getTestExplorationRandomUsersFile());

    return createBundleFile(testDefinitionFileSource, temporaryFolder);

  }

  public static File getTestExplorationOneAtATimeMemoryBundleFile(TemporaryFolder temporaryFolder)
      throws IOException {

    FileSource testDefinitionFileSource =
        new FileSource(TestFiles.getTestExplorationOneAtATimeMemoryFile().getName(),
            TestFiles.getTestExplorationOneAtATimeMemoryFile());

    return createBundleFile(testDefinitionFileSource, temporaryFolder);

  }

  public static File getTestExplorationOneAtATimeUsersEnvironmentBundleFile(
      TemporaryFolder temporaryFolder) throws IOException {

    FileSource testDefinitionFileSource =
        new FileSource(TestFiles.getTestExplorationOneAtATimeUsersEnvironmentFile().getName(),
            TestFiles.getTestExplorationOneAtATimeUsersEnvironmentFile());

    return createBundleFile(testDefinitionFileSource, temporaryFolder);

  }

  public static File getTestTerminationCriteriaBundleFile(TemporaryFolder temporaryFolder)
      throws IOException {

    FileSource testDefinitionFileSource =
        new FileSource(TestFiles.getTestTerminationCriteriaFile().getName(),
            TestFiles.getTestTerminationCriteriaFile());

    return createBundleFile(testDefinitionFileSource, temporaryFolder);

  }

  public static File getTestStepUsersBundleFile(TemporaryFolder temporaryFolder)
      throws IOException {

    FileSource testDefinitionFileSource = new FileSource(TestFiles.getTestStepUsersFile().getName(),
        TestFiles.getTestStepUsersFile());

    return createBundleFile(testDefinitionFileSource, temporaryFolder);

  }



  /**
   * Get a valid test bundle as an InputStream
   *
   * @param temporaryFolder where to create the zip file
   * @return the InputStream with the test bundle
   * @throws IOException if zip file could not be created
   */
  public static InputStream getLoadTestBundleInputStream(TemporaryFolder temporaryFolder)
      throws IOException {

    return new FileInputStream(getLoadTestBundleFile(temporaryFolder));

  }

  /**
   * Get a valid test bundle as a ZipInputStream
   *
   * @param temporaryFolder where to create the zip file
   * @return the ZipInputStream with the test bundle
   * @throws IOException if zip file could not be created
   */
  public static ZipInputStream getLoadTestBundleZipInputStream(TemporaryFolder temporaryFolder)
      throws IOException {

    return new ZipInputStream(getLoadTestBundleInputStream(temporaryFolder));
  }

  /**
   * Get an invalid valid test bundle (missing test definition) as a File
   *
   * @param temporaryFolder where to create the zip file
   * @return the File with the test bundle
   * @throws IOException if zip file could not be created
   */
  public static File getMissingTestDefinitionTestBundleFile(TemporaryFolder temporaryFolder)
      throws IOException {

    // setup the bundle contents without test definition
    ZipEntrySource[] addedEntries =
        new ZipEntrySource[] {new FileSource(TestFiles.getTestDeploymentDescriptorFile().getName(),
            TestFiles.getTestDeploymentDescriptorFile())};

    return createBundleFromZipEntrySource(addedEntries, temporaryFolder);

  }

  /**
   * Get an invalid valid test bundle (missing test definition) as an InputStream
   *
   * @param temporaryFolder where to create the zip file
   * @return the InputStream with the invalid test bundle
   * @throws IOException if zip file could not be created
   */
  public static InputStream getMissingTestDefinitionTestBundle(TemporaryFolder temporaryFolder)
      throws IOException {

    return new FileInputStream(getMissingTestDefinitionTestBundleFile(temporaryFolder));
  }

  /**
   * Get the test definition from the valid test bundle
   *
   * @return the String of the test definition
   * @throws IOException if file could not be found
   */
  public static String getValidTestDefinitionString() throws IOException {
    return IOUtils.toString(FileUtils.openInputStream(TestFiles.getTestLoadFile()),
        StandardCharsets.UTF_8);
  }

  /**
   * Get the deployment descriptor from the valid test bundle
   *
   * @return the String of the deployment descriptor
   * @throws IOException if file could not be found
   */
  public static String getValidDeploymentDescriptorString() throws IOException {
    return IOUtils.toString(TestFiles.getDeploymentDescriptor(), StandardCharsets.UTF_8);
  }

  /**
   * Get the test definition from the valid test bundle
   *
   * @return the InputStream of the test definition
   * @throws IOException if file could not be found
   */
  public static InputStream getValidTestDefinitionInputStream() throws IOException {

    return FileUtils.openInputStream(TestFiles.getTestLoadFile());

  }

  /**
   * Get the deployment descriptor from the valid test bundle
   *
   * @return the InputStream of the deployment descriptor
   * @throws IOException if file could not be found
   */
  public static InputStream getValidDeploymentDescriptorInputStream() throws IOException {

    return TestFiles.getDeploymentDescriptor();
  }

  /**
   * Get the models from the valid test bundle
   *
   * @return Map with the name of the model and the InputStream
   * @throws IOException if files could not be found
   */
  public static Map<String, InputStream> getValidBPMNModels() throws IOException {

    Map<String, InputStream> models = new HashMap<>();

    File[] files = TestFiles.getModelsFolderFile().listFiles();

    assert files != null;

    for (File file : files) {
      models.put(file.getName(), FileUtils.openInputStream(file));
    }

    return models;
  }

  private static File createBundleFile(FileSource testDefinitionFileSource,
      TemporaryFolder temporaryFolder) throws IOException {

    // setup the bundle contents
    ZipEntrySource[] addedEntries = new ZipEntrySource[] {testDefinitionFileSource,
        new FileSource(TestFiles.getTestDeploymentDescriptorFile().getName(),
            TestFiles.getTestDeploymentDescriptorFile())};

    return createBundleFromZipEntrySource(addedEntries, temporaryFolder);

  }

  private static File createBundleFromZipEntrySource(ZipEntrySource[] zipEntrySources,
      TemporaryFolder temporaryFolder) throws IOException {

    File zipFile =
        temporaryFolder.newFile(new BigInteger(130, new SecureRandom()).toString(32) + ".zip");

    ZipUtil.pack(zipEntrySources, zipFile);

    addModelsToZipFile(zipFile);

    return zipFile;

  }

  private static void addModelsToZipFile(File zipFile) {

    // add the models
    File[] files = TestFiles.getModelsFolderFile().listFiles();

    assert files != null;

    Arrays.stream(files).forEach(modelFile -> ZipUtil.addEntry(zipFile, new FileSource(
        BenchFlowConstants.BPMN_MODELS_FOLDER_NAME + "/" + modelFile.getName(), modelFile)));

  }
}
