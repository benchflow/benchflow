package cloud.benchflow.faban.client.responses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 30/10/15.
 */
public class RunQueue implements Response, Iterable<RunId> {

  private List<RunId> queue;

  public RunQueue() {
    queue = new ArrayList<>();
  }

  public void add(RunId runId) {
    queue.add(runId);
  }

  @Override
  public Iterator<RunId> iterator() {
    return queue.iterator();
  }

  @Override
  public void forEach(Consumer<? super RunId> action) {
    queue.forEach(action);
  }

  @Override
  public Spliterator<RunId> spliterator() {
    return queue.spliterator();
  }

  public boolean contains(RunId id) {
    return queue.contains(id);
  }

}
