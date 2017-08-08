package cloud.benchflow.testmanager.scheduler;

import cloud.benchflow.testmanager.tasks.AbortableCallable;
import cloud.benchflow.testmanager.tasks.AbortableRunnable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * See https://github.com/umermansoor/custom-future-wrapping-callable/blob/master/src/futuretaskwrapperconcept/FutureTaskWrapperConcept.java.
 * @author Vincenzo Ferme <info@vincenzoferme.it>
 */
public class CustomFutureReturningExecutor extends ThreadPoolExecutor {

  public CustomFutureReturningExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  public CustomFutureReturningExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
  }

  public CustomFutureReturningExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
  }

  public CustomFutureReturningExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
      RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
  }

  @Override
  public Future<?> submit(Runnable task) {
    if (task == null) {
      throw new IllegalArgumentException();
    }
    RunnableFuture<Void> ftask = newTaskFor(task, null);
    execute(ftask);
    return ftask;
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    if (task == null) {
      throw new IllegalArgumentException();
    }
    RunnableFuture<T> ftask = newTaskFor(task);
    execute(ftask);
    return ftask;
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
    if (callable instanceof AbortableCallable) {
      return ((AbortableCallable<T>) callable).newTask();
    } else {
      return super.newTaskFor(callable); // A regular Callable, delegate to parent
    }
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T result) {
    if (runnable instanceof AbortableRunnable) {
      return ((AbortableRunnable<T>) runnable).newTask();
    } else {
      return super.newTaskFor(runnable, result); // A regular Runnable, delegate to parent
    }
  }

}
