package cloud.benchflow.testmanager.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Provides an Abortable Abstract FutureTask Class.
 *
 * We do not rely on FutureTask because we need to directly control the state of the
 * task, to be marked as aborted, even when its execution completed because we cancel
 * the task using testTask.cancel(false);
 *
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public abstract class AbortableFutureTask<T> extends FutureTask<T> {

  public AbortableFutureTask(Callable<T> callable) {
    super(callable);
  }

  public AbortableFutureTask(Runnable runnable, T result) {
    super(runnable, result);
  }

  public abstract boolean isAborted();

  public abstract void abortTask();

}
