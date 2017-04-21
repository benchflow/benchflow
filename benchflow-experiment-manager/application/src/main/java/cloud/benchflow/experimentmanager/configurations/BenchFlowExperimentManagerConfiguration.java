package cloud.benchflow.experimentmanager.configurations;

import cloud.benchflow.experimentmanager.configurations.factory.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BenchFlowExperimentManagerConfiguration extends Configuration {

    // see http://www.dropwizard.io/1.0.6/docs/manual/core.html#configuration

    // Jersey Client Configuration
    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @JsonProperty("jerseyClient")
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }

    @JsonProperty("jerseyClient")
    public void setJerseyClient(JerseyClientConfiguration jerseyClient) {
        this.jerseyClient = jerseyClient;
    }

    // Swagger Configuration
    @Valid
    @NotNull
    private final SwaggerBundleConfiguration swagger = new SwaggerBundleConfiguration();

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration getSwagger() {
        return swagger;
    }

    // BenchFlow Environment Configuration
    @Valid
    @NotNull
    @JsonProperty
    private BenchFlowEnvironmentFactory benchFlowEnvironmentFactory = new BenchFlowEnvironmentFactory();

    @JsonProperty("benchFlowEnvironment")
    public BenchFlowEnvironmentFactory getBenchFlowEnvironmentFactory() {
        return benchFlowEnvironmentFactory;
    }

    @JsonProperty("benchFlowEnvironment")
    public void setBenchFlowEnvironmentFactory(BenchFlowEnvironmentFactory benchFlowEnvironmentFactory) {
        this.benchFlowEnvironmentFactory = benchFlowEnvironmentFactory;
    }

    // MongoDB Configuration
    @Valid
    @NotNull
    private MongoDBFactory mongoDBFactory = new MongoDBFactory();

    @JsonProperty("mongoDB")
    public MongoDBFactory getMongoDBFactory() {
        return mongoDBFactory;
    }

    @JsonProperty("mongoDB")
    public void setMongoDBFactory(MongoDBFactory mongoDBFactory) {
        this.mongoDBFactory = mongoDBFactory;
    }

    // Minio Service
    @Valid
    @NotNull
    @JsonProperty
    private MinioServiceFactory minioServiceFactory = new MinioServiceFactory();

    @JsonProperty("minio")
    public MinioServiceFactory getMinioServiceFactory() {
        return minioServiceFactory;
    }

    @JsonProperty("minio")
    public void setMinioServiceFactory(MinioServiceFactory minioServiceFactory) {
        this.minioServiceFactory = minioServiceFactory;
    }

    // Faban
    @Valid
    @NotNull
    private FabanServiceFactory fabanServiceFactory = new FabanServiceFactory();

    @JsonProperty("faban")
    public FabanServiceFactory getFabanServiceFactory() {
        return fabanServiceFactory;
    }

    @JsonProperty("faban")
    public void setFabanServiceFactory(FabanServiceFactory fabanServiceFactory) {
        this.fabanServiceFactory = fabanServiceFactory;
    }

    // Drivers Maker
    @Valid
    @NotNull
    private DriversMakerServiceFactory driversMakerServiceFactory = new DriversMakerServiceFactory();

    @JsonProperty("driversMaker")
    public DriversMakerServiceFactory getDriversMakerServiceFactory() {
        return driversMakerServiceFactory;
    }

    @JsonProperty("driversMaker")
    public void setDriversMakerServiceFactory(DriversMakerServiceFactory driversMakerServiceFactory) {
        this.driversMakerServiceFactory = driversMakerServiceFactory;
    }

    // Performance-Test-Manager Service
    @Valid
    @NotNull
    private TestManagerServiceFactory TestManagerServiceFactory = new TestManagerServiceFactory();

    @JsonProperty("testManager")
    public TestManagerServiceFactory getTestManagerServiceFactory() {
        return TestManagerServiceFactory;
    }

    @JsonProperty("testManager")
    public void setTestManagerServiceFactory(TestManagerServiceFactory testManagerServiceFactory) {
        this.TestManagerServiceFactory = testManagerServiceFactory;
    }

    // Task Executor
    @Valid
    @NotNull
    private TaskExecutorFactory taskExecutorServiceFactory = new TaskExecutorFactory();

    @JsonProperty("taskExecutor")
    public TaskExecutorFactory getTaskExecutorFactory() {
        return taskExecutorServiceFactory;
    }

    @JsonProperty("taskExecutor")
    public void setTaskExecutorFactory(TaskExecutorFactory taskExecutorServiceFactory) {
        this.taskExecutorServiceFactory = taskExecutorServiceFactory;
    }
}