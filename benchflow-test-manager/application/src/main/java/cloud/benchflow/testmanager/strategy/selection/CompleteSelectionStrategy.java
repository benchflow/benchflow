package cloud.benchflow.testmanager.strategy.selection;

import cloud.benchflow.dsl.BenchFlowDSL;
import cloud.benchflow.dsl.definition.BenchFlowExperiment;
import cloud.benchflow.dsl.definition.errorhandling.BenchFlowDeserializationException;
import cloud.benchflow.testmanager.BenchFlowTestManagerApplication;
import cloud.benchflow.testmanager.services.external.MinioService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-20
 */
public class CompleteSelectionStrategy implements ExperimentSelectionStrategy {

    private static Logger logger = LoggerFactory.getLogger(CompleteSelectionStrategy.class.getSimpleName());

    private MinioService minioService;

    public CompleteSelectionStrategy() {

        this.minioService = BenchFlowTestManagerApplication.getMinioService();

    }

    @Override
    public String selectNextExperiment(String testID) {


        try {

            String testDefinitionYamlString = IOUtils.toString(minioService.getTestDefinition(testID), StandardCharsets.UTF_8);

            // TODO - change so that this is generated based test goal
            BenchFlowExperiment experiment = BenchFlowDSL.experimentFromTestYaml(testDefinitionYamlString);

            // TODO - determine exploration space

            // TODO - check which experiments have been executed

            // TODO - select next experiment to execute

            // TODO - generate Experiment YAML file

            return BenchFlowDSL.experimentToYamlString(experiment);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (BenchFlowDeserializationException e) {
            // TODO - handle me
            e.printStackTrace();
        }

        return null;
    }
}
