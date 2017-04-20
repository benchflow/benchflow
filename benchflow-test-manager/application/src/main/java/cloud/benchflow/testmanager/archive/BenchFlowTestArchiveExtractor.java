package cloud.benchflow.testmanager.archive;

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

import static cloud.benchflow.testmanager.constants.BenchFlowConstants.*;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 15.02.17.
 */
public class BenchFlowTestArchiveExtractor {

//    benchflow-test.yaml or .yml
//    docker-compose.yaml or .yml
//    optionally the model folder (only for WfMSs)

    private enum ReturnType { STRING, INPUT_STREAM }

    private static BiPredicate<ZipEntry, String> entryMatches = (zipEntry, regEx) -> zipEntry.getName().matches(regEx);

    private static String definitionRegEx = getYamlRegExFromName(TEST_EXPERIMENT_DEFINITION_NAME);
    private static Predicate<ZipEntry> isExpConfig = entry -> entryMatches.test(entry, definitionRegEx);

    private static String deploymentDescriptorRegEx = getYamlRegExFromName(DEPLOYMENT_DESCRIPTOR_NAME);
    private static Predicate<ZipEntry> isDeploymentDescriptor = entry -> entryMatches.test(entry, deploymentDescriptorRegEx);

    public static String extractBenchFlowTestDefinitionString(ZipInputStream benchFlowTestArchive) throws IOException {

        return (String) extractBenchFlowTestDefinitionObject(benchFlowTestArchive, ReturnType.STRING, isExpConfig);

    }

    public static InputStream extractBenchFlowTestDefinitionInputStream(ZipInputStream benchFlowTestArchive) throws IOException {

        return (InputStream) extractBenchFlowTestDefinitionObject(benchFlowTestArchive, ReturnType.INPUT_STREAM, isExpConfig);

    }

    public static InputStream extractDeploymentDescriptorInputStream(ZipInputStream benchFlowTestArchive) throws IOException {

        return (InputStream) extractBenchFlowTestDefinitionObject(benchFlowTestArchive, ReturnType.INPUT_STREAM, isDeploymentDescriptor);

    }

    public static String extractDeploymentDescriptorString(ZipInputStream benchFlowTestArchive) throws IOException {

        return (String) extractBenchFlowTestDefinitionObject(benchFlowTestArchive, ReturnType.STRING, isDeploymentDescriptor);

    }

    private static Object extractBenchFlowTestDefinitionObject(ZipInputStream benchFlowTestArchive, ReturnType returnType, Predicate<ZipEntry> isFile) throws IOException {

        ZipEntry zipEntry;

        while ((zipEntry = benchFlowTestArchive.getNextEntry()) != null) {

            if (isFile.test(zipEntry)) {

                switch (returnType) {
                    case INPUT_STREAM:
                        return readZipEntryToInputStream(benchFlowTestArchive);
                    case STRING:
                        return readZipEntryToString(benchFlowTestArchive);
                }

            }
        }

        return null;

    }

    public static Map<String, InputStream> extractBPMNModelInputStreams(ZipInputStream benchFlowTestArchive) throws IOException {

        // TODO - validate that the names are the same as in the test definition

        BiPredicate<ZipEntry, String> isBPMNModelEntry = (zipEntry, string) ->
                zipEntry.getName().contains(string) && !zipEntry.getName().contains("._");

        Predicate<ZipEntry> isBPMNModel = entry -> isBPMNModelEntry.test(entry, BPMN_MODELS_FOLDER_NAME + "/");

        Map<String, InputStream> models = new HashMap<>();


        ZipEntry zipEntry;

        while ((zipEntry = benchFlowTestArchive.getNextEntry()) != null) {

            if (!zipEntry.isDirectory() && isBPMNModel.test(zipEntry)) {

                String fileName = zipEntry.getName().substring(zipEntry.getName().lastIndexOf("/") + 1);

                InputStream data = readZipEntryToInputStream(benchFlowTestArchive);

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

    private static InputStream readZipEntryToInputStream(ZipInputStream inputStream) throws IOException {

        ByteArrayInputStream resultInputStream = new ByteArrayInputStream(
                readZipEntryToOutputStream(inputStream).toByteArray());

        return resultInputStream;

    }

    private static ByteArrayOutputStream readZipEntryToOutputStream(ZipInputStream inputStream) throws IOException {

        byte[] buffer = new byte[1024];

        int len;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while ((len = inputStream.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }

        return out;

    }
}
