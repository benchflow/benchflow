package cloud.benchflow.experimentmanager.tasks;

import java.util.concurrent.Callable;

/**
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
/*
 * TODO - this and other Concurrency related classes should be moved in a shared library, since they
 * are used also in the test-manager
 */
public abstract class AbortableCallable<T> implements Callable<T> {

  private volatile boolean aborted = false;

  public AbortableFutureTask<T> newTask() {
    return new AbortableFutureTask<T>(this) {
      //NOTE: Here for future references, useful if we need to cancel the task
      //      @Override
      //      public boolean cancel(boolean mayInterruptIfRunning) {
      //
      //        boolean cancel = super.cancel(mayInterruptIfRunning);
      //        AbortableCallable.this.abortTask();
      //        return cancel;
      //
      //      }

      public void abortTask() {
        AbortableCallable.this.abortTask();
      }

      @Override
      public boolean isAborted() {
        return aborted;
      }
    };
  }

  public synchronized void abortTask() {
    aborted = true; // Set the cancellation flag
  }

}
