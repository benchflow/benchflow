package cloud.benchflow.experimentmanager.tasks;

import org.apache.commons.lang3.mutable.MutableBoolean;

/** @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-19 */
public abstract class CancellableTask implements Runnable {

  protected MutableBoolean isCanceled = new MutableBoolean(false);

  public void cancel() {
    isCanceled.setTrue();
  }
}
