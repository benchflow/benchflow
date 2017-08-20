package cloud.benchflow.experimentmanager;

import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.experimentmanager.resources.TrialResource;
import cloud.benchflow.experimentmanager.scheduler.CustomFutureReturningExecutor;
import cloud.benchflow.experimentmanager.scheduler.ExperimentTaskScheduler;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.external.faban.FabanManagerService;
import cloud.benchflow.experimentmanager.services.external.test.FabanManagerServiceMock;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.faban.client.FabanClient;
import com.mongodb.MongoClient;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundleConfiguration;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import javax.ws.rs.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchFlowExperimentManagerApplication
    extends Application<BenchFlowExperimentManagerConfiguration> {

  private static BenchFlowExperimentModelDAO experimentModelDAO;
  private static TrialModelDAO trialModelDAO;
  private static MinioService minioService;
  private static FabanManagerService fabanManagerService;
  private static DriversMakerService driversMakerService;
  private static BenchFlowTestManagerService testManagerService;
  private static ExperimentTaskScheduler experimentTaskScheduler;
  private static int submitRetries;
  private static String minioServiceAddress;
  private static String fabanManagerServiceAddress;
  private Logger logger =
      LoggerFactory.getLogger(BenchFlowExperimentManagerApplication.class.getSimpleName());

  // TODO - remove when faban manager is separate service
  private static TrialResource trialResource;

  public static void main(String[] args) throws Exception {
    new BenchFlowExperimentManagerApplication().run(args);
  }

  public static BenchFlowExperimentModelDAO getExperimentModelDAO() {
    return experimentModelDAO;
  }

  public static TrialModelDAO getTrialModelDAO() {
    return trialModelDAO;
  }

  public static MinioService getMinioService() {
    return minioService;
  }

  // used for testing to insert mock/spy object
  public static void setMinioService(MinioService minioService) {
    BenchFlowExperimentManagerApplication.minioService = minioService;
  }

  public static FabanManagerService getFabanManagerService() {
    return fabanManagerService;
  }

  // used for testing to insert mock/spy object
  public static void setFabanManagerService(FabanManagerService fabanManagerService) {
    BenchFlowExperimentManagerApplication.fabanManagerService = fabanManagerService;
  }

  public static DriversMakerService getDriversMakerService() {
    return driversMakerService;
  }

  // used for testing to insert mock/spy object
  public static void setDriversMakerService(DriversMakerService driversMakerService) {
    BenchFlowExperimentManagerApplication.driversMakerService = driversMakerService;
  }

  public static BenchFlowTestManagerService getTestManagerService() {
    return testManagerService;
  }

  // used for testing to insert mock/spy object
  public static void setTestManagerService(BenchFlowTestManagerService testManagerService) {
    BenchFlowExperimentManagerApplication.testManagerService = testManagerService;
  }

  public static ExperimentTaskScheduler getExperimentTaskScheduler() {
    return experimentTaskScheduler;
  }

  public static void setExperimentTaskScheduler(ExperimentTaskScheduler experimentTaskScheduler) {
    BenchFlowExperimentManagerApplication.experimentTaskScheduler = experimentTaskScheduler;
  }

  public static int getSubmitRetries() {
    return submitRetries;
  }

  public static String getMinioServiceAddress() {
    return minioServiceAddress;
  }

  public static String getFabanManagerServiceAddress() {
    return fabanManagerServiceAddress;
  }

  public static TrialResource getTrialResource() {
    return trialResource;
  }

  @Override
  public String getName() {
    return "benchflow-experiment-manager";
  }

  @Override
  public void initialize(Bootstrap<BenchFlowExperimentManagerConfiguration> bootstrap) {

    logger.info("initialize");

    // Dropwizard Template Config
    bootstrap.addBundle(new TemplateConfigBundle(
        new TemplateConfigBundleConfiguration().resourceIncludePath("/app")));

    // Dropwizard Swagger
    bootstrap.addBundle(new SwaggerBundle<BenchFlowExperimentManagerConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
          BenchFlowExperimentManagerConfiguration configuration) {
        return configuration.getSwagger();
      }
    });
  }

  @Override
  public void run(BenchFlowExperimentManagerConfiguration configuration, Environment environment)
      throws Exception {

    logger.info("run");

    // external services addresses
    minioServiceAddress = configuration.getMinioServiceFactory().getAddress();
    fabanManagerServiceAddress = configuration.getFabanServiceFactory().getAddress();

    final Client client = new JerseyClientBuilder(environment)
        .using(configuration.getJerseyClientConfiguration()).build(environment.getName());

    MongoClient mongoClient = configuration.getMongoDBFactory().build();
    FabanClient fabanClient = configuration.getFabanServiceFactory().build();

    // services
    CustomFutureReturningExecutor taskExecutor =
        configuration.getExperimentTaskExecutorFactory().build(environment);

    experimentTaskScheduler = new ExperimentTaskScheduler(taskExecutor);

    experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient);
    trialModelDAO = new TrialModelDAO(mongoClient);

    minioService = configuration.getMinioServiceFactory().build();
    driversMakerService = configuration.getDriversMakerServiceFactory().build(client);
    testManagerService = configuration.getTestManagerServiceFactory().build(client);

    // start in test mode or not
    if (configuration.getTestModeFactory().isMockFaban()) {
      fabanManagerService = new FabanManagerServiceMock(fabanClient);
    } else {
      fabanManagerService = configuration.getFabanManagerServiceFactory().build(fabanClient);
    }

    submitRetries = configuration.getFabanServiceFactory().getSubmitRetries();

    // initialize to fetch dependencies
    fabanManagerService.initialize();
    experimentTaskScheduler.initialize();

    // make sure a bucket exists
    minioService.initializeBuckets();

    // instantiate resources
    BenchFlowExperimentResource experimentResource = new BenchFlowExperimentResource();
    trialResource = new TrialResource();

    // TODO - health checks for all services
    //        final TemplateHealthCheck healthCheck =
    //                new TemplateHealthCheck(configuration.getTemplate());
    //        environment.healthChecks().register("template", healthCheck);

    // register resources
    environment.jersey().register(experimentResource);
    environment.jersey().register(trialResource);
  }
}
