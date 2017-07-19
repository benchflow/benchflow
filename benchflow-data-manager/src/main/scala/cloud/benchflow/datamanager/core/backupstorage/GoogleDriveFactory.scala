package cloud.benchflow.datamanager.core.backupstorage

import com.typesafe.config.ConfigFactory

class GoogleDriveFromConfig extends GoogleDriveBackupStorage {
  lazy val configuration = ConfigFactory.load()
  override lazy val baseFolderName: String = configuration.getString("googledrive.baseFolder")
  override lazy val googleDrive: GoogleDrive = {
    val applicationName = configuration.getString("googledrive.appname")
    val secret = configuration.getString("googledrive.secret")
    val credentialsDir = configuration.getString("googledrive.credentials_dir")

    val drive = new GoogleDrive(applicationName, secret, credentialsDir)

    // TODO: this should be connected to an actor
    //    drive.setUploadProgressListener(new MediaHttpUploaderProgressListener {
    //      override def progressChanged(uploader: MediaHttpUploader) =
    //        uploader.getUploadState() match {
    //          case INITIATION_STARTED =>
    //            logger.debug("Upload Initiation has started.");
    //          case INITIATION_COMPLETE =>
    //            logger.debug("Upload Initiation is Complete.");
    //          case MEDIA_IN_PROGRESS =>
    //            logger.debug("Upload is In Progress: "
    //              + NumberFormat.getPercentInstance().format(uploader.getProgress()));
    //          case MEDIA_COMPLETE =>
    //            logger.debug("Upload is Complete!");
    //        }
    //    })

    drive
  }

}

class GoogleDriveImpl(val googleDrive: GoogleDrive, val baseFolderName: String) extends GoogleDriveBackupStorage

