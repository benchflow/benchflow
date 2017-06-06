package cloud.benchflow.experimentmanager.configurations.factory;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 * @author jesper (jesper.findahl@usi.ch) - Created on 07/03/16.
 */
public class TaskExecutorFactory {

  // TODO - same as on TestManager (move to common library)

  @NotNull
  @Min(1)
  private int minThreads;
  @NotNull
  @Min(1)
  private int maxThreads;

  @JsonProperty
  public int getMinThreads() {
    return minThreads;
  }

  @JsonProperty
  public void setMinThreads(int minThreads) {
    this.minThreads = minThreads;
  }

  @JsonProperty
  public int getMaxThreads() {
    return maxThreads;
  }

  @JsonProperty
  public void setMaxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
  }

  public ExecutorService build(Environment environment) {

    int cpus = Runtime.getRuntime().availableProcessors();

    return environment.lifecycle().executorService("task-%d").minThreads(minThreads * cpus)
        .maxThreads(maxThreads * cpus).keepAliveTime(Duration.seconds(60))
        .workQueue(new SynchronousQueue<>()).threadFactory(new DaemonThreadFactory())
        .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()).build();
  }

  /**
   * See http://dev.bizo.com/2014/06/cached-thread-pool-considered-harmlful.html
   */
  public static class DaemonThreadFactory implements ThreadFactory {

    private AtomicInteger count = new AtomicInteger();

    @Override
    public Thread newThread(@Nonnull Runnable r) {
      Thread thread = new Thread(r);
      thread.setName("experiment-manager-" + count.incrementAndGet());
      thread.setDaemon(true);
      return thread;
    }
  }
}
