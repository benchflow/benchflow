package cloud.benchflow.testmanager.tasks;

/**
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public abstract class AbortableRunnable<T> implements Runnable {

  private volatile boolean aborted = false;

  public AbortableFutureTask<T> newTask() {
    return new AbortableFutureTask<T>(this, null) {
      //NOTE: Here for future references, useful if we need to cancel the task
      //      @Override
      //      public boolean cancel(boolean mayInterruptIfRunning) {
      //
      //        boolean cancel = super.cancel(mayInterruptIfRunning);
      //        AbortableRunnable.this.abortTask();
      //        return cancel;
      //
      //      }

      public void abortTask() {
        AbortableRunnable.this.abortTask();
      }

      @Override
      public boolean isAborted() {
        return aborted;
      }
    };
  }

  public synchronized void abortTask() {
    aborted = true; // Set the abortion flag
  }

}
