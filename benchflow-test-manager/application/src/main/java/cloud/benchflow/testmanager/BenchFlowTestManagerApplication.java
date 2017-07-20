package cloud.benchflow.testmanager;

import cloud.benchflow.testmanager.configurations.BenchFlowTestManagerConfiguration;
import cloud.benchflow.testmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.testmanager.resources.BenchFlowTestResource;
import cloud.benchflow.testmanager.resources.BenchFlowTrialResource;
import cloud.benchflow.testmanager.scheduler.TestTaskScheduler;
import cloud.benchflow.testmanager.services.external.BenchFlowExperimentManagerService;
import cloud.benchflow.testmanager.services.external.MinioService;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.BenchFlowTestModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.ExplorationModelDAO;
import cloud.benchflow.testmanager.services.internal.dao.UserDAO;
import com.mongodb.MongoClient;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundleConfiguration;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.concurrent.ExecutorService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchFlowTestManagerApplication
    extends Application<BenchFlowTestManagerConfiguration> {

  private static BenchFlowTestModelDAO testModelDAO;
  private static ExplorationModelDAO explorationModelDAO;
  private static BenchFlowExperimentModelDAO experimentModelDAO;
  private static UserDAO userDAO;
  private static MinioService minioService;
  private static BenchFlowExperimentManagerService experimentManagerService;
  private static TestTaskScheduler testTaskScheduler;
  private static String minioServiceAddress;
  private static String experimentManagerAddress;
  private Logger logger =
      LoggerFactory.getLogger(BenchFlowTestManagerApplication.class.getSimpleName());

  public static void main(String[] args) throws Exception {
    new BenchFlowTestManagerApplication().run(args);
  }

  public static BenchFlowTestModelDAO getTestModelDAO() {
    return testModelDAO;
  }

  public static ExplorationModelDAO getExplorationModelDAO() {
    return explorationModelDAO;
  }

  public static BenchFlowExperimentModelDAO getExperimentModelDAO() {
    return experimentModelDAO;
  }

  public static UserDAO getUserDAO() {
    return userDAO;
  }

  public static MinioService getMinioService() {
    return minioService;
  }

  public static BenchFlowExperimentManagerService getExperimentManagerService() {
    return experimentManagerService;
  }

  // used for testing to insert mock/spy object
  public static void setExperimentManagerService(
      BenchFlowExperimentManagerService experimentManagerService) {
    BenchFlowTestManagerApplication.experimentManagerService = experimentManagerService;
  }

  public static TestTaskScheduler getTestTaskScheduler() {
    return testTaskScheduler;
  }

  public static String getMinioServiceAddress() {
    return minioServiceAddress;
  }

  public static String getExperimentManagerAddress() {
    return experimentManagerAddress;
  }

  @Override
  public String getName() {
    return "benchflow-test-orchestrator";
  }

  @Override
  public void initialize(Bootstrap<BenchFlowTestManagerConfiguration> bootstrap) {

    logger.info("initialize");

    // Dropwizard Template Config
    bootstrap.addBundle(new TemplateConfigBundle(
        new TemplateConfigBundleConfiguration().resourceIncludePath("/app")));

    // Dropwizard Swagger
    bootstrap.addBundle(new SwaggerBundle<BenchFlowTestManagerConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
          BenchFlowTestManagerConfiguration configuration) {
        return configuration.getSwagger();
      }
    });
  }

  @Override
  public void run(BenchFlowTestManagerConfiguration configuration, Environment environment)
      throws Exception {

    logger.info("run");

    // external services addresses
    minioServiceAddress = configuration.getMinioServiceFactory().getAddress();
    experimentManagerAddress =
        "http://" + configuration.getBenchFlowExperimentManagerServiceFactory().getAddress();

    // services

    // Typically you only create one MongoClient instance for a given MongoDB deployment
    // (e.g. standalone, replica set, or a sharded cluster) and use it across your application.
    // http://mongodb.github.io/mongo-java-driver/3.4/driver/getting-started/quick-start/
    MongoClient mongoClient = configuration.getMongoDBFactory().build();
    ExecutorService taskExecutor = configuration.getTaskExecutorFactory().build(environment);

    testModelDAO = new BenchFlowTestModelDAO(mongoClient);
    explorationModelDAO = new ExplorationModelDAO(mongoClient, testModelDAO);
    experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient, testModelDAO);
    userDAO = new UserDAO(mongoClient, testModelDAO);

    minioService = configuration.getMinioServiceFactory().build();
    experimentManagerService = configuration.getBenchFlowExperimentManagerServiceFactory()
        .build(configuration, environment);

    testTaskScheduler = new TestTaskScheduler(taskExecutor);

    // initialize to fetch dependencies
    testTaskScheduler.initialize();

    // make sure a bucket exists
    minioService.initializeBuckets();

    // resources
    //        final BenchFlowUserResource userResource = new BenchFlowUserResource();

    final BenchFlowTestResource testResource = new BenchFlowTestResource();
    final BenchFlowExperimentResource experimentResource = new BenchFlowExperimentResource();
    final BenchFlowTrialResource trialResource = new BenchFlowTrialResource();

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
