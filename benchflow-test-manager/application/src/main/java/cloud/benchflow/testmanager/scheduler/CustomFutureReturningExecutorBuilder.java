package cloud.benchflow.testmanager.scheduler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.util.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Customises io.dropwizard.lifecycle.setup.ExecutorServiceBuilder to handle the
 * CustomFutureReturningExecutor in a managed way by Dropwizard.
 *
 * Most of the code comes from the referenced ExecutorServiceBuilder. I needed to copy it because
 * the field of ExecutorServiceBuilder are private and there are no setter and getter.
 *
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public class CustomFutureReturningExecutorBuilder {

  private static Logger log = LoggerFactory.getLogger(CustomFutureReturningExecutorBuilder.class);

  private final LifecycleEnvironment environment;
  private final String nameFormat;
  private int corePoolSize;
  private int maximumPoolSize;
  private Duration keepAliveTime;
  private Duration shutdownTime;
  private BlockingQueue<Runnable> workQueue;
  private ThreadFactory threadFactory;
  private RejectedExecutionHandler handler;

  /**
   * Constructs a CustomFutureReturningExecutorBuilder.
   * @param environment the lifecycle environment
   * @param nameFormat the name format
   * @param factory the thread factory
   */
  public CustomFutureReturningExecutorBuilder(LifecycleEnvironment environment, String nameFormat,
      ThreadFactory factory) {
    this.environment = environment;
    this.nameFormat = nameFormat;
    this.corePoolSize = 0;
    this.maximumPoolSize = 1;
    this.keepAliveTime = Duration.seconds(60);
    this.shutdownTime = Duration.seconds(5);
    this.workQueue = new LinkedBlockingQueue<>();
    this.threadFactory = factory;
    this.handler = new ThreadPoolExecutor.AbortPolicy();
  }

  /**
   * Constructs a CustomFutureReturningExecutorBuilder.
   * @param environment the lifecycle environment
   * @param nameFormat the name format
   */
  public CustomFutureReturningExecutorBuilder(LifecycleEnvironment environment, String nameFormat) {
    this(environment, nameFormat, new ThreadFactoryBuilder().setNameFormat(nameFormat).build());
  }

  public CustomFutureReturningExecutorBuilder minThreads(int threads) {
    this.corePoolSize = threads;
    return this;
  }

  public CustomFutureReturningExecutorBuilder maxThreads(int threads) {
    this.maximumPoolSize = threads;
    return this;
  }

  public CustomFutureReturningExecutorBuilder keepAliveTime(Duration time) {
    this.keepAliveTime = time;
    return this;
  }

  public CustomFutureReturningExecutorBuilder shutdownTime(Duration time) {
    this.shutdownTime = time;
    return this;
  }

  public CustomFutureReturningExecutorBuilder workQueue(BlockingQueue<Runnable> workQueue) {
    this.workQueue = workQueue;
    return this;
  }

  public CustomFutureReturningExecutorBuilder rejectedExecutionHandler(
      RejectedExecutionHandler handler) {
    this.handler = handler;
    return this;
  }

  public CustomFutureReturningExecutorBuilder threadFactory(ThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
    return this;
  }

  /**
   * Builds a CustomFutureReturningExecutorBuilder.
   * @return a CustomFutureReturningExecutor
   */
  public CustomFutureReturningExecutor build() {
    if (corePoolSize != maximumPoolSize && maximumPoolSize > 1 && !isBoundedQueue()) {
      log.warn("Parameter 'maximumPoolSize' is conflicting with unbounded work queues");
    }
    final CustomFutureReturningExecutor executor = new CustomFutureReturningExecutor(corePoolSize,
        maximumPoolSize, keepAliveTime.getQuantity(), keepAliveTime.getUnit(), workQueue,
        threadFactory, handler);
    environment.manage(new ExecutorServiceManager(executor, shutdownTime, nameFormat));
    return executor;
  }

  private boolean isBoundedQueue() {
    return workQueue.remainingCapacity() != Integer.MAX_VALUE;
  }

  @VisibleForTesting
  static synchronized void setLog(Logger newLog) {
    log = newLog;
  }
}
