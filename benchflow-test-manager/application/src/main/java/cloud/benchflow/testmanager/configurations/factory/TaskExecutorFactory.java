package cloud.benchflow.testmanager.configurations.factory;

import cloud.benchflow.testmanager.scheduler.CustomFutureReturningExecutor;
import cloud.benchflow.testmanager.scheduler.CustomFutureReturningExecutorBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 19.12.16.
 */
public final class TaskExecutorFactory {

  @NotNull
  @Min(1)
  private int minThreads;

  @JsonProperty
  public int getMinThreads() {
    return minThreads;
  }

  @JsonProperty
  public void setMinThreads(int minThreads) {
    this.minThreads = minThreads;
  }

  @NotNull
  @Min(1)
  private int maxThreads;

  @JsonProperty
  public int getMaxThreads() {
    return maxThreads;
  }

  @JsonProperty
  public void setMaxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
  }

  /**
   * Build executor service.
   *
   * @param environment application environment
   * @return CustomFutureReturningExecutor
   */
  public CustomFutureReturningExecutor build(Environment environment) {

    int processors = Runtime.getRuntime().availableProcessors();

    CustomFutureReturningExecutorBuilder customFutureReturningExecutorBuilder =
        new CustomFutureReturningExecutorBuilder(environment.lifecycle(), "task-%d");

    return customFutureReturningExecutorBuilder.minThreads(minThreads * processors)
        .maxThreads(maxThreads * processors).keepAliveTime(Duration.seconds(60))
        .workQueue(new SynchronousQueue<>()).threadFactory(new DaemonThreadFactory())
        .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()).build();
  }

  /** See http://dev.bizo.com/2014/06/cached-thread-pool-considered-harmlful.html */
  private class DaemonThreadFactory implements ThreadFactory {

    private AtomicInteger count = new AtomicInteger();

    @Override
    public Thread newThread(@Nonnull Runnable r) {
      Thread thread = new Thread(r);
      thread.setName("benchflow-test-manager-" + count.incrementAndGet());
      thread.setDaemon(true);
      return thread;
    }
  }
}
