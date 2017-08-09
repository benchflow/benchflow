package cloud.benchflow.testmanager.bundle;

import cloud.benchflow.testmanager.helpers.constants.TestBundle;
import cloud.benchflow.testmanager.helpers.constants.TestConstants;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 16.02.17.
 */
public class BenchFlowTestBundleExtractorTest {

  // needs to be subfolder of current folder for Wercker
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));

  @Test
  public void extractBenchFlowTestDefinition() throws Exception {

    String testDefinitionString = BenchFlowTestBundleExtractor
        .extractBenchFlowTestDefinitionString(TestBundle.getValidTestBundleZip(temporaryFolder));

    Assert.assertNotNull(testDefinitionString);

    Assert.assertTrue(testDefinitionString.contains("version:"));
    Assert.assertTrue(testDefinitionString.contains("name: " + TestConstants.LOAD_TEST_NAME));
  }

  @Test
  public void extractDeploymentDescriptor() throws Exception {

    InputStream deploymentDescriptorInputStream = BenchFlowTestBundleExtractor
        .extractDeploymentDescriptorInputStream(TestBundle.getValidTestBundleZip(temporaryFolder));

    Assert.assertNotNull(deploymentDescriptorInputStream);

    String deploymentDescriptorString = org.apache.commons.io.IOUtils
        .toString(deploymentDescriptorInputStream, StandardCharsets.UTF_8.name());

    Assert.assertTrue(deploymentDescriptorString.contains("version:"));
  }

  @Test
  public void extractBPMNModels() throws Exception {

    int numberOfModels = TestBundle.BPMN_MODELS_COUNT;

    Map<String, InputStream> bpmnModels = BenchFlowTestBundleExtractor
        .extractBPMNModelInputStreams(TestBundle.getValidTestBundleZip(temporaryFolder));

    Assert.assertEquals(numberOfModels, bpmnModels.size());
  }
}
