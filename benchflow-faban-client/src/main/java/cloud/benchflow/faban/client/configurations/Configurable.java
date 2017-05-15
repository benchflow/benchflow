package cloud.benchflow.faban.client.configurations;

/**
 * An abstract class representing a configurable object.
 * 
 * @author Simone D'Avico (simonedavico@gmail.com) - Created on 26/10/15.
 */
@SuppressWarnings("unchecked")
public abstract class Configurable<U extends Config> {

  protected U config;

  public <T extends Configurable> T withConfig(U config) {
    this.config = config;
    return (T) this;
  }

}
