package cloud.benchflow.experimentmanager;

import cloud.benchflow.experimentmanager.configurations.BenchFlowExperimentManagerConfiguration;
import cloud.benchflow.experimentmanager.resources.BenchFlowExperimentResource;
import cloud.benchflow.experimentmanager.services.external.BenchFlowTestManagerService;
import cloud.benchflow.experimentmanager.services.external.DriversMakerService;
import cloud.benchflow.experimentmanager.services.external.FabanManagerService;
import cloud.benchflow.experimentmanager.services.external.MinioService;
import cloud.benchflow.experimentmanager.services.internal.dao.BenchFlowExperimentModelDAO;
import cloud.benchflow.experimentmanager.services.internal.dao.TrialModelDAO;
import cloud.benchflow.experimentmanager.scheduler.ExperimentTaskScheduler;
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

import java.util.concurrent.ExecutorService;

import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchFlowExperimentManagerApplication
    extends Application<BenchFlowExperimentManagerConfiguration> {

  private Logger logger =
      LoggerFactory.getLogger(BenchFlowExperimentManagerApplication.class.getSimpleName());

  private static BenchFlowExperimentModelDAO experimentModelDAO;
  private static TrialModelDAO trialModelDAO;
  private static MinioService minioService;
  private static FabanClient fabanClient;
  private static FabanManagerService fabanManagerService;
  private static DriversMakerService driversMakerService;
  private static BenchFlowTestManagerService testManagerService;
  private static ExperimentTaskScheduler experimentTaskScheduler;
  private static int submitRetries;

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

  public static FabanClient getFabanClient() {
    return fabanClient;
  }

  public static FabanManagerService getFabanManagerService() {
    return fabanManagerService;
  }

  public static DriversMakerService getDriversMakerService() {
    return driversMakerService;
  }

  public static BenchFlowTestManagerService getTestManagerService() {
    return testManagerService;
  }

  public static ExperimentTaskScheduler getExperimentTaskScheduler() {
    return experimentTaskScheduler;
  }

  // used for testing to insert mock/spy object
  public static void setFabanClient(FabanClient fabanClient) {
    BenchFlowExperimentManagerApplication.fabanClient = fabanClient;
  }

  // used for testing to insert mock/spy object
  public static void setFabanManagerService(FabanManagerService fabanManagerService) {
    BenchFlowExperimentManagerApplication.fabanManagerService = fabanManagerService;
  }

  // used for testing to insert mock/spy object
  public static void setDriversMakerService(DriversMakerService driversMakerService) {
    BenchFlowExperimentManagerApplication.driversMakerService = driversMakerService;
  }

  // used for testing to insert mock/spy object
  public static void setTestManagerService(BenchFlowTestManagerService testManagerService) {
    BenchFlowExperimentManagerApplication.testManagerService = testManagerService;
  }

  // used for testing to insert mock/spy object
  public static void setMinioService(MinioService minioService) {
    BenchFlowExperimentManagerApplication.minioService = minioService;
  }

  public static int getSubmitRetries() {
    return submitRetries;
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

    Client client = new JerseyClientBuilder(environment)
        .using(configuration.getJerseyClientConfiguration()).build(environment.getName());

    MongoClient mongoClient = configuration.getMongoDBFactory().build();

    // services
    ExecutorService experimentTaskExecutorService =
        configuration.getExperimentTaskExecutorFactory().build(environment);

    experimentModelDAO = new BenchFlowExperimentModelDAO(mongoClient);
    trialModelDAO = new TrialModelDAO(mongoClient);

    minioService = configuration.getMinioServiceFactory().build();
    // TODO - remove and only use FabanManagerService
    fabanClient = configuration.getFabanServiceFactory().build();
    fabanManagerService = new FabanManagerService(fabanClient);
    driversMakerService = configuration.getDriversMakerServiceFactory().build(client);
    testManagerService = configuration.getTestManagerServiceFactory().build(client);

    submitRetries = configuration.getFabanServiceFactory().getSubmitRetries();

    // ensure it is last so other services have been assigned
    experimentTaskScheduler = new ExperimentTaskScheduler(experimentTaskExecutorService);

    // make sure a bucket exists
    minioService.initializeBuckets();

    // instantiate resources
    BenchFlowExperimentResource experimentResource = new BenchFlowExperimentResource();

    // TODO - health checks for all services
    //        final TemplateHealthCheck healthCheck =
    //                new TemplateHealthCheck(configuration.getTemplate());
    //        environment.healthChecks().register("template", healthCheck);

    // register resources
    environment.jersey().register(experimentResource);
  }
}
