package cloud.benchflow.experimentmanager.tasks;

import cloud.benchflow.experimentmanager.BenchFlowExperimentManagerApplication;
import cloud.benchflow.experimentmanager.DockerComposeIT;
import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.constants.BenchFlowConstants;
import cloud.benchflow.experimentmanager.helpers.MinioTestData;
import cloud.benchflow.experimentmanager.helpers.TestConstants;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunStatus;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 2017-04-14
 */
public class RunBenchFlowExperimentTaskIT extends DockerComposeIT {

    private static final int TEST_PORT = 8080;
    private static final String TEST_ADDRESS = "localhost:" + TEST_PORT;

    @Rule
    public final DropwizardAppRule<BenchFlowExperimentManagerConfiguration> RULE = new DropwizardAppRule<>(
            BenchFlowExperimentManagerApplication.class,
            "../configuration.yml",
            ConfigOverride.config("mongoDB.hostname", MONGO_CONTAINER.getIp()),
            ConfigOverride.config("mongoDB.port", String.valueOf(MONGO_CONTAINER.getExternalPort())),
            ConfigOverride.config("minio.address", "http://" + MINIO_CONTAINER.getIp() + ":" + MINIO_CONTAINER.getExternalPort()),
            ConfigOverride.config("minio.accessKey", MINIO_ACCESS_KEY),
            ConfigOverride.config("minio.secretKey", MINIO_SECRET_KEY),
            ConfigOverride.config("driversMaker.address", TEST_ADDRESS),
            ConfigOverride.config("testManager.address", TEST_ADDRESS),
            ConfigOverride.config("faban.address", TEST_ADDRESS)
    );

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(TEST_PORT);

    private String experimentID = TestConstants.BENCHFLOW_EXPERIMENT_ID;
    private RunBenchFlowExperimentTask runExperimentTask;
    private FabanClient fabanClientMock;

    @Before
    public void setUp() throws Exception {

        BenchFlowExperimentManagerConfiguration configuration = RULE.getConfiguration();
        Environment environment = RULE.getEnvironment();

        RunBenchFlowExperimentTask.DriversMakerCompatibleID driversMakerCompatibleID = new RunBenchFlowExperimentTask.DriversMakerCompatibleID().invoke(experimentID);

        Client client = new JerseyClientBuilder(environment)
                .using(configuration.getJerseyClientConfiguration())
                .build("experiment-manager-test");

        BenchFlowExperimentModelDAO experimentModelDAO = new BenchFlowExperimentModelDAO(configuration.getMongoDBFactory().build());

        // spy on minio to return files saved by other services
        MinioService minioService = Mockito.spy(configuration.getMinioServiceFactory().build());
        Mockito.doReturn(MinioTestData.getExperimentDefinition())
                .when(minioService)
                .getExperimentDefinition(experimentID);
        Mockito.doReturn(MinioTestData.getDeploymentDescriptor())
                .when(minioService)
                .getExperimentDeploymentDescriptor(experimentID);
        Mockito.doReturn(MinioTestData.get11ParallelStructuredModel())
                .when(minioService)
                .getExperimentBPMNModel(experimentID, MinioTestData.BPM_MODEL_11_PARALLEL_NAME);
        Mockito.doReturn(MinioTestData.getGeneratedBenchmark())
                .when(minioService)
                .getDriversMakerGeneratedBenchmark(driversMakerCompatibleID.getDriversMakerExperimentID(), driversMakerCompatibleID.getExperimentNumber());
        Mockito.doReturn(MinioTestData.getFabanConfiguration())
                .when(minioService)
                .getDriversMakerGeneratedFabanConfiguration(driversMakerCompatibleID.getDriversMakerExperimentID(), driversMakerCompatibleID.getExperimentNumber(), 1);

        // mock faban because interaction is difficult to capture
        fabanClientMock = Mockito.mock(FabanClient.class);

        DriversMakerService driversMakerService = configuration.getDriversMakerServiceFactory().build(client);
        BenchFlowTestManagerService testManagerService = configuration.getTestManagerServiceFactory().build(client);

        int submitRetries = configuration.getFabanServiceFactory().getSubmitRetries();

        runExperimentTask = new RunBenchFlowExperimentTask(
                experimentID,
                experimentModelDAO,
                minioService,
                fabanClientMock,
                driversMakerService,
                testManagerService,
                submitRetries
        );

    }

    @Test
    public void run() throws Exception {

        String fabanID = "test_faban_id";
        RunId runId = new RunId(fabanID, "1");
        RunStatus status = new RunStatus("COMPLETED", runId);
        String trialID = experimentID + BenchFlowConstants.MODEL_ID_DELIMITER + 1;

        // Drivers Maker Stub
        stubFor(post(urlEqualTo(DriversMakerService.GENERATE_BENCHMARK_PATH))
                .willReturn(aResponse().withStatus(Response.Status.OK.getStatusCode())
                )
        );

        // Test Manager Trial Status Stub
        stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromTrialID(trialID) + BenchFlowTestManagerService.TRIAL_STATUS_PATH))
                .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())
                )
        );

        // Test Manager Experiment State Stub
        stubFor(put(urlEqualTo(BenchFlowConstants.getPathFromExperimentID(experimentID) + BenchFlowTestManagerService.EXPERIMENT_STATE_PATH))
                .willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())
                )
        );

        Mockito.doReturn(runId)
                .when(fabanClientMock)
                .submit(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class));

        Mockito.doReturn(status)
                .when(fabanClientMock)
                .status(runId);


        runExperimentTask.run();

    }
}