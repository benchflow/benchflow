package cloud.benchflow.testmanager;

import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.testmanager.resources.BenchFlowTestResource;
import cloud.benchflow.testmanager.resources.BenchFlowTrialResource;
import cloud.benchflow.testmanager.resources.BenchFlowUserResource;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundleConfiguration;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public class BenchFlowTestManagerApplication extends Application<BenchFlowTestManagerConfiguration> {

    private Logger logger = LoggerFactory.getLogger(BenchFlowTestManagerApplication.class.getSimpleName());

    public static void main(String[] args) throws Exception {
        new BenchFlowTestManagerApplication().run(args);
    }

    @Override
    public String getName() {
        return "benchflow-test-orchestrator";
    }

    @Override
    public void initialize(Bootstrap<BenchFlowTestManagerConfiguration> bootstrap) {

        logger.info("initialize");

        // Dropwizard Template Config
        bootstrap.addBundle(new TemplateConfigBundle(new TemplateConfigBundleConfiguration().resourceIncludePath("/app")));

        // Dropwizard Swagger
        bootstrap.addBundle(new SwaggerBundle<BenchFlowTestManagerConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(BenchFlowTestManagerConfiguration configuration) {
                return configuration.getSwagger();
            }
        });

    }

    @Override
    public void run(BenchFlowTestManagerConfiguration configuration, Environment environment) throws Exception {

        logger.info("run");

        // services
        ExecutorService taskExecutor = configuration.getTaskExecutorFactory().build(environment);
        BenchFlowTestModelDAO testModelDAO = new BenchFlowTestModelDAO(configuration.getMongoDBFactory().build());
        BenchFlowExperimentModelDAO experimentModelDAO = new BenchFlowExperimentModelDAO(configuration.getMongoDBFactory().build(), testModelDAO);
        UserDAO userDAO = new UserDAO(configuration.getMongoDBFactory().build(), testModelDAO);
        MinioService minioService = configuration.getMinioServiceFactory().build();
        BenchFlowExperimentManagerService experimentManagerService = configuration.getBenchFlowExperimentManagerServiceFactory().build(
                configuration, environment);

        // make sure a bucket exists
        minioService.initializeBuckets();


        // resources
//        final BenchFlowUserResource userResource = new BenchFlowUserResource();

        final BenchFlowTestResource testResource = new BenchFlowTestResource(taskExecutor, minioService, testModelDAO, experimentModelDAO, userDAO, experimentManagerService);
        final BenchFlowExperimentResource experimentResource = new BenchFlowExperimentResource(experimentModelDAO);
        final BenchFlowTrialResource trialResource = new BenchFlowTrialResource(experimentModelDAO);

        // TODO - health checks for all services
//        final TemplateHealthCheck healthCheck =
//                new TemplateHealthCheck(configuration.getTemplate());
//        environment.healthChecks().register("template", healthCheck);

//        environment.jersey().register(userResource);
        environment.jersey().register(testResource);
        environment.jersey().register(experimentResource);
        environment.jersey().register(trialResource);

        // add support for submitting files
        environment.jersey().register(MultiPartFeature.class);

    }


}
