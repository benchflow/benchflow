package cloud.benchflow.datamanager.core.datarepository.filestorage

import java.io.InputStream

import cloud.benchflow.datamanager.core.datarepository.objectstorage.MinioFromConfig

case class ExperimentFile(name: String, content: InputStream, length: Long, contentType: String)

trait ExperimentFileStorage {
  def getExperimentFiles(experimentId: String): List[ExperimentFile]
  def putExperimentFile(file: ExperimentFile): Unit
  def experimentFileNames(experimentId: String): List[String]
}
