package cloud.benchflow.testmanager.bundle;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 16.02.17.
 */
public class BenchFlowTestBundleExtractorTest {
  @Before
  public void setUp() throws Exception {}

  @Test
  public void extractBenchFlowTestDefinition() throws Exception {

    String ptDefinition = BenchFlowTestBundleExtractor
        .extractBenchFlowTestDefinitionString(TestBundle.getInValidTestBundleZip());

    Assert.assertNotNull(ptDefinition);

    Assert.assertTrue(ptDefinition.contains("version:"));
  }

  @Test
  public void extractDeploymentDescriptor() throws Exception {

    InputStream deploymentDescriptorInputStream = BenchFlowTestBundleExtractor
        .extractDeploymentDescriptorInputStream(TestBundle.getValidTestBundleZip());

    Assert.assertNotNull(deploymentDescriptorInputStream);

    String deploymentDescriptorString = org.apache.commons.io.IOUtils
        .toString(deploymentDescriptorInputStream, StandardCharsets.UTF_8.name());

    Assert.assertTrue(deploymentDescriptorString.contains("version:"));
  }

  @Test
  public void extractBPMNModels() throws Exception {

    int numberOfModels = TestBundle.BPMN_MODELS_COUNT;

    Map<String, InputStream> bpmnModels = BenchFlowTestBundleExtractor
        .extractBPMNModelInputStreams(TestBundle.getValidTestBundleZip());

    Assert.assertEquals(numberOfModels, bpmnModels.size());
  }
}
