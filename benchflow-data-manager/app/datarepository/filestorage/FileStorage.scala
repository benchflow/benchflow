package datarepository.filestorage

import java.io.InputStream

import com.google.inject.ImplementedBy

import datarepository.objectstorage.MinioFromConfig

case class ExperimentFile(name: String, content: InputStream, length: Long, contentType: String)

@ImplementedBy(classOf[MinioFromConfig])
trait ExperimentFileStorage {
  def getExperimentFiles(experimentId: String): List[ExperimentFile]
  def putExperimentFile(file: ExperimentFile): Unit
  def experimentFileNames(experimentId: String): List[String]
}
