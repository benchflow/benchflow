package cloud.benchflow.experimentmanager;

import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.helpers.MinioTestData;
import cloud.benchflow.experimentmanager.helpers.TestConstants;
import cloud.benchflow.experimentmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-13
 */
public class BenchFlowExperimentManagerApplicationIT extends DockerComposeIT {

    @Rule
    public final DropwizardAppRule<BenchFlowExperimentManagerConfiguration> RULE = new DropwizardAppRule<>(
            BenchFlowExperimentManagerApplication.class,
            "../configuration.yml",
            ConfigOverride.config("mongoDB.hostname", MONGO_CONTAINER.getIp()),
            ConfigOverride.config("mongoDB.port", String.valueOf(MONGO_CONTAINER.getExternalPort())),
            ConfigOverride.config("minio.address", "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort()),
            ConfigOverride.config("minio.accessKey", MINIO_ACCESS_KEY),
            ConfigOverride.config("minio.secretKey", MINIO_SECRET_KEY)
    );

    private String experimentID = TestConstants.BENCHFLOW_EXPERIMENT_ID;
    private MinioService minioService;

    @Before
    public void setUp() throws Exception {
        minioService = RULE.getConfiguration().getMinioServiceFactory().build();
    }

    @Test
    public void runValidExperiment() throws Exception {

        // make sure experiment has been saved to minio
        minioService.saveExperimentDefinition(experimentID, MinioTestData.getExperimentDefinition());

        Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

        // about hardcoding localhost: no way to get IP and also in the dropwizard examples they have it hardcoded
        // https://github.com/dropwizard/dropwizard/blob/master/dropwizard-example/src/test/java/com/example/helloworld/IntegrationTest.java#L46
        Response response = client.target(String.format("http://localhost:%d", RULE.getLocalPort()))
                .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
                .path(BenchFlowExperimentResource.RUN_ACTION_PATH)
                .request()
                .post(null);

        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());


    }

    @Test
    public void runInvalidExperiment() throws Exception {

        // experiment has not been saved to minio

        Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

        // about hardcoding localhost: no way to get IP and also in the dropwizard examples they have it hardcoded
        // https://github.com/dropwizard/dropwizard/blob/master/dropwizard-example/src/test/java/com/example/helloworld/IntegrationTest.java#L46
        Response response = client.target(String.format("http://localhost:%d", RULE.getLocalPort()))
                .path(BenchFlowConstants.getPathFromExperimentID(experimentID))
                .path(BenchFlowExperimentResource.RUN_ACTION_PATH)
                .request()
                .post(null);

        Assert.assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());

    }

}