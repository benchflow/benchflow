package cloud.benchflow.experimentmanager.helpers;

/**
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public interface WaitExperimentCheck {

  void checkExperimentIsFinished() throws InterruptedException;
}
