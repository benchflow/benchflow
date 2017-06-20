package cloud.benchflow.faban.client;

import cloud.benchflow.faban.client.commands.DeployCommand;
import cloud.benchflow.faban.client.commands.KillCommand;
import cloud.benchflow.faban.client.commands.PendingCommand;
import cloud.benchflow.faban.client.commands.RunInfoCommand;
import cloud.benchflow.faban.client.commands.ShowLogsCommand;
import cloud.benchflow.faban.client.commands.StatusCommand;
import cloud.benchflow.faban.client.commands.SubmitCommand;
import cloud.benchflow.faban.client.configurations.Configurable;
import cloud.benchflow.faban.client.configurations.DeployConfig;
import cloud.benchflow.faban.client.configurations.FabanClientConfig;
import cloud.benchflow.faban.client.configurations.FabanClientDefaultConfig;
import cloud.benchflow.faban.client.configurations.RunConfig;
import cloud.benchflow.faban.client.configurations.ShowLogsConfig;
import cloud.benchflow.faban.client.configurations.SubmitConfig;
import cloud.benchflow.faban.client.exceptions.BenchmarkNameNotFoundException;
import cloud.benchflow.faban.client.exceptions.ConfigFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.FabanClientException;
import cloud.benchflow.faban.client.exceptions.JarFileNotFoundException;
import cloud.benchflow.faban.client.exceptions.RunIdNotFoundException;
import cloud.benchflow.faban.client.responses.DeployStatus;
import cloud.benchflow.faban.client.responses.RunId;
import cloud.benchflow.faban.client.responses.RunInfo;
import cloud.benchflow.faban.client.responses.RunLogStream;
import cloud.benchflow.faban.client.responses.RunQueue;
import cloud.benchflow.faban.client.responses.RunStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The faban client implementation.
 *
 * @author Simone D'Avico (simonedavico@gmail.com)
 * @author vincenzoferme
 */
@SuppressWarnings("unused")
public class FabanClient extends Configurable<FabanClientConfig> {

  private FabanClientConfig defaultConfig = new FabanClientDefaultConfig();

  private FabanClientConfig chooseConfig() {
    return (config == null) ? defaultConfig : config;
  }

  /**
   * Deploy a Faban benchmark.
   *
   * @param jarFile the benchmark to be deployed on the faban harness
   * @return a response enclosing the status of the operation
   */
  public DeployStatus deploy(File jarFile) throws FabanClientException, JarFileNotFoundException {

    String benchmarkName = jarFile.getName();
    DeployConfig deployConfig = new DeployConfig(jarFile, benchmarkName);
    DeployCommand deploy = new DeployCommand().withConfig(deployConfig);
    FabanClientConfig fabanConfig = chooseConfig();

    try (FileInputStream fin = new FileInputStream(jarFile)) {

      return deploy.exec(fabanConfig);
      //return deploy(fin, jarFile.getName());

    } catch (FileNotFoundException e) {
      throw new JarFileNotFoundException(
          "The specified jar file ( " + jarFile.getAbsolutePath() + " could not be found.");
    } catch (IOException e) {
      throw new FabanClientException(
          "An unknow error occurred while processing the driver to deploy. " + "Please try again.",
          e);
    }

  }

  /**
   * Deploy a Faban benchmark, handling the result of the deployment with an handler.
   *
   * @param jarFile the benchmark to be deployed on the faban harness
   * @param driverName the name of the driver
   * @param handler a callback function
   * @return a response enclosing the status of the operation
   * @throws FabanClientException when there is an error interacting with faban
   * @throws JarFileNotFoundException when the benchmark jar is not found
   */
  public <R extends DeployStatus, T> T deploy(File jarFile, String driverName,
      Function<R, T> handler) throws FabanClientException, JarFileNotFoundException {
    return this.deploy(jarFile).handle(handler);
  }

  /**
   * Deploy a Faban benchmark, handling the result of the deployment with an handler.
   *
   * @param jarFile the benchmark to be deployed on the faban harness
   * @param driverName the name of the driver
   * @param handler a callback function
   * @throws FabanClientException when there is an error interacting with faban
   * @throws JarFileNotFoundException when the benchmark jar is not found
   */
  public <R extends DeployStatus> void deploy(File jarFile, String driverName, Consumer<R> handler)
      throws FabanClientException, JarFileNotFoundException {
    this.deploy(jarFile).handle(handler);
  }

  /**
   * Deploy a Faban benchmark, handling the result of the deployment with an handler.
   *
   * @param jarFile the benchmark to be deployed on the faban harness
   * @param handler a function that receives a {@link DeployStatus} and returns a {@code <T>}
   * @param <R> the type of the handler input (has to extend {@link DeployStatus})
   * @param <T> the arbitrary return type
   * @return an object of type {@code <T>}
   */
  public <R extends DeployStatus, T> T deploy(File jarFile, Function<R, T> handler)
      throws JarFileNotFoundException, FabanClientException {
    return this.deploy(jarFile).handle(handler);
  }


  /**
   * Deploy a Faban benchmark, handling the result of the deployment with an handler.
   *
   * @param jarFile the benchmark to be deployed on the faban harness
   * @param handler a consumer that receives a {@link DeployStatus}
   * @param <R> the type of the handler input (has to extend {@link DeployStatus})
   * @throws FabanClientException when there is an error interacting with faban
   */
  public <R extends DeployStatus> void deploy(File jarFile, Consumer<R> handler)
      throws JarFileNotFoundException, FabanClientException {
    this.deploy(jarFile).handle(handler);
  }

  /**
   * Get the status of a Faban benchmark.
   *
   * @param runId a run id
   * @return a response enclosing the status of the operation
   * @throws FabanClientException when there is an error interacting with faban
   * @throws RunIdNotFoundException when the run id is not found
   */
  public RunStatus status(RunId runId) throws FabanClientException, RunIdNotFoundException {

    RunConfig runConfig = new RunConfig(runId);
    StatusCommand status = new StatusCommand().withConfig(runConfig);
    FabanClientConfig fabanConfig = chooseConfig();

    try {
      return status.exec(fabanConfig);
    } catch (IOException e) {
      throw new FabanClientException(
          "Something went wrong while requesting status with runId" + runId, e);
    }

  }

  /**
   * Get the status of a Faban benchmark, handling the result with an handler.
   *
   * @param runId a run id
   * @param handler a callback function
   * @param <R> The input to the {@param handler} function, a run status
   * @param <T> The return type of the {@param handler} function
   * @return An instance of {@link T}
   * @throws FabanClientException when there is an error interacting with faban
   * @throws RunIdNotFoundException if passed a non existent run id
   */
  public <R extends RunStatus, T> T status(RunId runId, Function<R, T> handler)
      throws FabanClientException, RunIdNotFoundException {
    return this.status(runId).handle(handler);
  }

  /**
   * Get the status of a Faban benchmark, handling the result with an handler.
   *
   * @param runId a run id
   * @param handler a callback function
   * @param <R> The input to the {@param handler} consumer, a run status
   * @throws RunIdNotFoundException when the run id is not found
   */
  public <R extends RunStatus> void status(RunId runId, Consumer<R> handler)
      throws FabanClientException, RunIdNotFoundException {
    this.status(runId).handle(handler);
  }

  /**
   * Get the Run Info of a Faban benchmark.
   *
   * @param runId a run id
   * @return a response enclosing the run Info of the operation
   * @throws FabanClientException when there is an error interacting with faban
   * @throws RunIdNotFoundException when the run id is not found
   */
  public RunInfo runInfo(RunId runId) throws FabanClientException, RunIdNotFoundException {

    RunConfig runConfig = new RunConfig(runId);
    RunInfoCommand runInfo = new RunInfoCommand().withConfig(runConfig);
    FabanClientConfig fabanConfig = chooseConfig();

    try {
      return runInfo.exec(fabanConfig);
    } catch (IOException e) {
      throw new FabanClientException(
          "Something went wrong while requesting run info with runId" + runId, e);
    }

  }

  /**
   * Get the Run Info of a Faban benchmark, handling the result with an handler.
   *
   * @param runId a run id
   * @param handler a callback function
   * @param <R> The input to the {@param handler} function, a run info
   * @param <T> The return type of the {@param handler} function
   * @return An instance of {@link T}
   * @throws FabanClientException when there is an error interacting with faban
   * @throws RunIdNotFoundException if passed a non existent run id
   */
  public <R extends RunInfo, T> T runInfo(RunId runId, Function<R, T> handler)
      throws FabanClientException, RunIdNotFoundException {
    return this.runInfo(runId).handle(handler);
  }

  /**
   * Get the Run Info of a Faban benchmark, handling the result with an handler.
   *
   * @param runId a run id
   * @param handler a callback function
   * @param <R> The input to the {@param handler} consumer, a run info
   * @throws RunIdNotFoundException when the run id is not found
   */
  public <R extends RunInfo> void runInfo(RunId runId, Consumer<R> handler)
      throws FabanClientException, RunIdNotFoundException {
    this.runInfo(runId).handle(handler);
  }

  /**
   * Submit a Faban benchmark.
   *
   * @param benchmarkName benchmark shortname
   * @param profile a profile name
   * @param configFile a config xml file for the run
   * @return the run id for the run
   * @throws FabanClientException when there is an error interacting with faban
   * @throws BenchmarkNameNotFoundException when the benchmark is not found
   */
  public RunId submit(String benchmarkName, String profile, InputStream configFile)
      throws FabanClientException, BenchmarkNameNotFoundException {

    SubmitConfig runConfig = new SubmitConfig(benchmarkName, profile, configFile);
    SubmitCommand submit = new SubmitCommand().withConfig(runConfig);
    FabanClientConfig fabanConfig = chooseConfig();

    try {
      return submit.exec(fabanConfig);
    } catch (IOException e) {
      throw new FabanClientException("Something went wrong while submitting the run for benchmark "
          + benchmarkName + " at profile " + profile);
    }

  }

  /**
   * Submit a Faban benchmark.
   *
   * @param benchmarkName benchmark shortname
   * @param profile a profile name
   * @param configFile a config xml file for the run
   * @return the run id for the run
   * @throws ConfigFileNotFoundException when the configuration file is not found
   * @throws BenchmarkNameNotFoundException when the benchmark is not found
   */
  public RunId submit(String benchmarkName, String profile, File configFile)
      throws FabanClientException, ConfigFileNotFoundException, BenchmarkNameNotFoundException {

    //if(configFile.exists()) {
    try (FileInputStream fin = new FileInputStream(configFile)) {

      return submit(benchmarkName, profile, fin);

    } catch (FileNotFoundException e) {
      throw new ConfigFileNotFoundException(
          "Configuration file " + configFile.getAbsolutePath() + " could not be found.");
    } catch (IOException e) {
      throw new FabanClientException("Something went wrong while submitting the run for benchmark "
          + benchmarkName + " at profile " + profile);
    }

  }

  /**
   * Submit a Faban benchmark, handling the result with an handler.
   *
   * @param benchmarkName benchmark shortname
   * @param profile a profile name
   * @param configFile a config xml file for the run
   * @param handler a callback function
   * @return a run id
   * @throws FabanClientException when there is an error interacting with faban
   * @throws ConfigFileNotFoundException when the configuration file is not found
   * @throws BenchmarkNameNotFoundException when the benchmark is not found
   */
  public <R extends RunId, T> T submit(String benchmarkName, String profile, InputStream configFile,
      Function<R, T> handler)
      throws FabanClientException, ConfigFileNotFoundException, BenchmarkNameNotFoundException {
    return this.submit(benchmarkName, profile, configFile).handle(handler);
  }

  /**
   * Submit a Faban benchmark, handling the result with an handler.
   *
   * @param benchmarkName benchmark shortname
   * @param profile a profile name
   * @param configFile a config xml file for the run
   * @param handler a callback function
   * @throws FabanClientException when there is an error interacting with faban
   * @throws ConfigFileNotFoundException when the configuration file is not found
   * @throws BenchmarkNameNotFoundException when the benchmark is not found
   */
  public <R extends RunId> void submit(String benchmarkName, String profile, InputStream configFile,
      Consumer<R> handler)
      throws FabanClientException, ConfigFileNotFoundException, BenchmarkNameNotFoundException {
    this.submit(benchmarkName, profile, configFile).handle(handler);
  }

  /**
   * Submit a Faban benchmark, handling the result with an handler.
   *
   * @param benchmarkName benchmark shortname
   * @param profile a profile name
   * @param configFile a config xml file for the run
   * @param handler a callback function {@link R} -> {@link T}
   * @param <R> a subclass of {@link RunId}
   * @param <T> return type of the {@param handler} function
   * @return the run id for the run
   * @throws ConfigFileNotFoundException when the configuration file is not found
   * @throws BenchmarkNameNotFoundException when the benchmark is not found
   */
  public <R extends RunId, T> T submit(String benchmarkName, String profile, File configFile,
      Function<R, T> handler)
      throws FabanClientException, ConfigFileNotFoundException, BenchmarkNameNotFoundException {
    return this.submit(benchmarkName, profile, configFile).handle(handler);
  }

  /**
   * Submit a Faban benchmark, handling the result with an handler.
   *
   * @param benchmarkName benchmark shortname
   * @param profile a profile name
   * @param configFile a config xml file
   * @param handler a callback consumer {@link R} -> void
   * @param <R> a subclass of {@link RunId}
   * @throws ConfigFileNotFoundException when the configuration file is not found
   * @throws BenchmarkNameNotFoundException when the benchmark is not found
   */
  public <R extends RunId> void submit(String benchmarkName, String profile, File configFile,
      Consumer<R> handler)
      throws FabanClientException, ConfigFileNotFoundException, BenchmarkNameNotFoundException {
    this.submit(benchmarkName, profile, configFile).handle(handler);
  }

  /**
   * Kill a running Faban benchmark.
   *
   * @param runId a run id
   * @return status of the kill operation
   * @throws RunIdNotFoundException when the run id is not found
   */
  public RunStatus kill(RunId runId) throws FabanClientException, RunIdNotFoundException {

    RunConfig killConfig = new RunConfig(runId);
    KillCommand kill = new KillCommand().withConfig(killConfig);
    FabanClientConfig fabanConfig = chooseConfig();

    try {
      return kill.exec(fabanConfig);
    } catch (IOException e) {
      throw new FabanClientException("Unexpected IO error while trying to kill " + runId, e);
    }

  }

  /**
   * Kill a running Faban benchmark, handling the result with an handler.
   *
   * @param runId a run id
   * @param handler a callback function {@link R} -> {@link T}
   * @param <R> a subclass of {@link RunStatus}
   * @param <T> return type of {@param handler}
   * @return an object of type {@link T}
   * @throws RunIdNotFoundException when the run id is not found
   */
  public <R extends RunStatus, T> T kill(RunId runId, Function<R, T> handler)
      throws RunIdNotFoundException, FabanClientException {
    return this.kill(runId).handle(handler);
  }

  /**
   * Kill a running Faban benchmark, handling the result with an handler.
   *
   * @param runId a run id
   * @param handler a callback consumer {@link R} -> void
   * @param <R> a subclass of {@link RunStatus}
   * @throws RunIdNotFoundException when the run id is not found
   */
  public <R extends RunStatus> void kill(RunId runId, Consumer<R> handler)
      throws RunIdNotFoundException, FabanClientException {
    this.kill(runId).handle(handler);
  }

  /**
   * Return the list of pending benchmarks.
   *
   * @return a queue of pending run ids
   */
  public RunQueue pending() throws FabanClientException {

    PendingCommand pending = new PendingCommand();
    FabanClientConfig fabanConfig = chooseConfig();

    try {
      return pending.exec(fabanConfig);
    } catch (IOException e) {
      throw new FabanClientException("Unexpected IO error while requesting for pending runs", e);
    }

  }

  /**
   * Return the list of pending benchmarks, handling the result with an handler.
   *
   * @param handler a callback function {@link R} -> {@link T}
   * @param <R> a subclass of {@link RunStatus}
   * @param <T> return type of {@param handler}
   * @return a queue of pending run ids
   */
  public <R extends RunQueue, T> T pending(Function<R, T> handler) {
    return this.pending().handle(handler);
  }

  /**
   * Return the list of pending benchmarks, handling the result with an handler.
   *
   * @param handler a callback consumer {@link R} -> void
   * @param <R> a subclass of {@link RunQueue}
   */
  public <R extends RunQueue> void pending(Consumer<R> handler) {
    this.pending().handle(handler);
  }

  /**
   * Show the logs of a running benchmark.
   *
   * @param runId the run id for the run
   * @return a LogStream
   * @throws FabanClientException when there is an error interacting with faban
   * @throws RunIdNotFoundException when the run id is not found
   */
  public RunLogStream showlogs(RunId runId) throws FabanClientException, RunIdNotFoundException {

    ShowLogsConfig logsConfig = new ShowLogsConfig(runId);
    ShowLogsCommand showlogs = new ShowLogsCommand().withConfig(logsConfig);
    FabanClientConfig fabanConfig = chooseConfig();

    try {
      return showlogs.exec(fabanConfig);
    } catch (IOException e) {
      throw new FabanClientException(
          "Something went wrong while retrieving the logs for run " + runId);
    }

  }

  /**
   * Show the logs of a running benchmark, handling the result with an handler.
   *
   * @param runId the run id for the run
   * @param handler a callback function
   * @return a LogStream
   * @throws FabanClientException when there is an error interacting with faban
   * @throws RunIdNotFoundException when the run id is not found
   */
  public <R extends RunLogStream, T> T showlogs(RunId runId, Function<R, T> handler)
      throws FabanClientException, RunIdNotFoundException {
    return this.showlogs(runId).handle(handler);
  }

  /**
   * Show the logs of a running benchmark, handling the result with an handler.
   *
   * @param runId the run id for the run
   * @param handler a callback function
   * @throws FabanClientException when there is an error interacting with faban
   * @throws RunIdNotFoundException when the run id is not found
   */
  public <R extends RunLogStream> void showlogs(RunId runId, Consumer<R> handler)
      throws FabanClientException, RunIdNotFoundException {
    this.showlogs(runId).handle(handler);
  }

}
