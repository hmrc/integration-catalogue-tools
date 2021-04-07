package uk.gov.hmrc.integrationcataloguetools

import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.charset.StandardCharsets

import scala.concurrent.Future

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import collection.JavaConverters._
import uk.gov.hmrc.integrationcataloguetools.models._

import uk.gov.hmrc.integrationcataloguetools.connectors.PublisherConnector

class FileTransferPublisherService(publisherConnector: PublisherConnector) {
  import scala.concurrent.ExecutionContext.Implicits._

  def publishDirectory(platform: Platform, directoryPath: String) : Either[String, Unit]= {
    
    def isJsonFile(file:File) : Boolean = {
      if (file.isDirectory()) return false
      else file.getName().endsWith(".json")
    }

    var directory = new File(directoryPath)
    if (!directory.isDirectory()){
      Left(s"`$directory` is not a directory")
    } else {
      val results = 
        directory
          .listFiles()
          .sortBy(_.getName())
          .filter(isJsonFile)
          .map(file => publishFile(platform, file.getPath()) )

      val lefts = results.collect({ case Left(l) => l})
      
      val rights = results.collect({ case Right(l) => l})

      println(s"Successfully published ${rights.length} File Transfers")
      if (lefts.nonEmpty){
        println(s"Failed to publish ${lefts.length} File Transfers")
      }

      if (lefts.isEmpty) Right(())
      else Left(lefts.mkString("\n"))
    }
  }

  def publishFile(platform :Platform, pathname: String) : Either[String, Unit] = {

    println(s"Publishing ${pathname}")

    val file = new File(pathname)
    val filename = file.getName()

    val fileContents = Files.readAllLines(Paths.get(pathname)).asScala.mkString

    publish(platform, publisherReference = PublisherReference(filename), filename, fileContents)
  }

  private def publish(platform: Platform, publisherReference: PublisherReference, filename: String, jsonString: String) : Either[String, Unit] = {
      val headers = Map("x-platform-type" -> platform.value)

      val responseEither = publisherConnector.publishFileTransfer(headers, jsonString);

      responseEither.flatMap(response => {
        response.statusCode match {
          case 200 | 201  => {
            println(s"Published. ${getApiPublishPhrase(response.statusCode)} API. Response(${response.statusCode}): ${response.content}") 
            Right(())
          }
          case otherStatusCode => {
            val errorMessage = s"Failed to publish '$filename'. Response(${response.statusCode}): ${response.content}"
            Left(errorMessage)
        }
      }
    })
  }

  def getApiPublishPhrase(statusCode: Int) : String = statusCode match {
      case 200 => "Updated"
      case 201 => "Created"
      case _ => "???"
  }
}

