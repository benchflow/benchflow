package cloud.benchflow.driversmaker.generation.utils;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 *
 * Created on 18/08/16.
 */
public interface Consumer<T> {

    void accept(T t);

}
