package cloud.benchflow.experimentmanager.exceptions;

/**
 * @author Simone D'Avico (simonedavico@gmail.com)
 * @author Jesper Findahl (jesper.findahl@usi.ch) - Created on 28/07/16.
 */
public class ArchiveExtractionException extends Exception {

  public static final String MESSAGE = "Couldn't extract experiment archive for ";

  public ArchiveExtractionException(String minioExperimentId, Throwable cause) {
    super(MESSAGE + minioExperimentId, cause);
  }
}
