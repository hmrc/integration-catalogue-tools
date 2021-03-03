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

import uk.gov.hmrc.integrationcataloguetools.connectors.PublisherConnector

case class PublisherReference(value: String) extends AnyVal
case class Platform(value: String) extends AnyVal

class PublisherService(publisherConnector: PublisherConnector) {
  import scala.concurrent.ExecutionContext.Implicits._

  def publishDirectory(platform: Platform, directoryPath: String) : Either[String, Unit]= {
    
    def isOasFile(file:File) : Boolean = {
      if (file.isDirectory()) return false
      else file.getName().endsWith(".yaml") || file.getName().endsWith(".json")
    }

    var directory = new File(directoryPath)
    if (!directory.isDirectory()){
      Left(s"$directory is not a directory")
    } else {
      val results = 
        directory
          .listFiles()
          .filter(isOasFile)
          .map(file => publishFile(platform, file.getPath()) )

      val lefts = results.collect({ case Left(l) => l})
      
      val rights = results.collect({ case Right(l) => l})

      println(s"Successfully published ${rights.length} ${platform.value} APIs")
      if (lefts.nonEmpty){
        println(s"Failed to publish ${lefts.length} APIs")
      }

      if (lefts.isEmpty) Right(())
      else Left(lefts.mkString("\n"))
    }
  }

  def publishFile(platform: Platform, pathname: String) : Either[String, Unit] = {

    println(s"Publishing ${pathname}")

    val file = new File(pathname)
    val filename = file.getName()

    val oasContentBytes = Files.readAllBytes(Paths.get(pathname))

    publish(platform,  publisherReference = PublisherReference(filename), filename, oasContentBytes)
  }

  private def publish(platform: Platform, publisherReference: PublisherReference, filename: String, oasContent: Array[Byte]) : Either[String, Unit] = {
      
    val headers = Map(
      "x-platform-type" -> platform.value,
      "x-specification-type" -> "OAS_V3",
      "x-publisher-reference" -> publisherReference.value)

      val responseEither = publisherConnector.publish(headers, filename, oasContent);

      responseEither.flatMap(response => {      
        response.statusCode match {
          case 200 => {
            println(s"Published. Updated API. Response(${response.statusCode}): ${response.content}") 
            Right(())
          }
          case 201 => {
            println(s"Published. Created API. Response(${response.statusCode}): ${response.content}") 
            Right(())
          }
          case otherStatusCode => {
            val errorMessage = s"Failed to publish '$filename'. Response(${response.statusCode}): ${response.content}"
            Left(errorMessage)
        }
      }
    })
  }
}

