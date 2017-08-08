package cloud.benchflow.testmanager.helpers;

/**
 * @author Vincenzo Ferme (info@vincenzoferme.it)
 */
public interface WaitTestCheck {
  void checkTestIsFinished() throws InterruptedException;
}
