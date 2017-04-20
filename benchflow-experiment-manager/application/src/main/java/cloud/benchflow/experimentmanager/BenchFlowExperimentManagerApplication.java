package cloud.benchflow.experimentmanager;

import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundleConfiguration;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.util.concurrent.ExecutorService;

public class BenchFlowExperimentManagerApplication extends Application<BenchFlowExperimentManagerConfiguration> {

    private Logger logger = LoggerFactory.getLogger(BenchFlowExperimentManagerApplication.class.getSimpleName());

    public static void main(String[] args) throws Exception {
        new BenchFlowExperimentManagerApplication().run(args);
    }

    @Override
    public String getName() {
        return "benchflow-experiment-manager";
    }

    @Override
    public void initialize(Bootstrap<BenchFlowExperimentManagerConfiguration> bootstrap) {

        logger.info("initialize");

        // Dropwizard Template Config
        bootstrap.addBundle(new TemplateConfigBundle(new TemplateConfigBundleConfiguration().resourceIncludePath("/app")));

        // Dropwizard Swagger
        bootstrap.addBundle(new SwaggerBundle<BenchFlowExperimentManagerConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(BenchFlowExperimentManagerConfiguration configuration) {
                return configuration.getSwagger();
            }
        });

    }

    @Override
    public void run(BenchFlowExperimentManagerConfiguration configuration, Environment environment) throws Exception {

        logger.info("run");

        Client client = new JerseyClientBuilder(environment)
                .using(configuration.getJerseyClientConfiguration())
                .build(environment.getName());

        // services
        ExecutorService taskExecutorService = configuration.getTaskExecutorFactory().build(environment);
        BenchFlowExperimentModelDAO experimentModelDAO = new BenchFlowExperimentModelDAO(configuration.getMongoDBFactory().build());
        MinioService minioService = configuration.getMinioServiceFactory().build();
        FabanClient fabanClient = configuration.getFabanServiceFactory().build();
        DriversMakerService driversMakerService = configuration.getDriversMakerServiceFactory().build(client);
        BenchFlowTestManagerService testManagerService = configuration.getTestManagerServiceFactory().build(client);

        int submitRetries = configuration.getFabanServiceFactory().getSubmitRetries();

        // make sure a bucket exists
        minioService.initializeBuckets();

        // instantiate resources
        BenchFlowExperimentResource experimentResource = new BenchFlowExperimentResource(
                minioService,
                experimentModelDAO,
                fabanClient,
                driversMakerService,
                taskExecutorService,
                testManagerService,
                submitRetries
        );



        // TODO - health checks for all services
//        final TemplateHealthCheck healthCheck =
//                new TemplateHealthCheck(configuration.getTemplate());
//        environment.healthChecks().register("template", healthCheck);

        // register resources
        environment.jersey().register(experimentResource);

    }


}
