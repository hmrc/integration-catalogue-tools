package uk.gov.hmrc.integrationcataloguetools.service

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

import uk.gov.hmrc.integrationcataloguetools.models._
import uk.gov.hmrc.integrationcataloguetools.connectors.PublisherConnector

class ApiPublisherService(publisherConnector: PublisherConnector) {
  import scala.concurrent.ExecutionContext.Implicits._

  def publishDirectory(directoryPath: String, useFilenameAsPublisherReference: Boolean): Either[String, Unit] = {

    def isOasFile(file: File): Boolean = {
      if (file.isDirectory()) return false
      else file.getName().endsWith(".yaml") || file.getName().endsWith(".json")
    }

    val directory = new File(directoryPath)
    if (!directory.isDirectory()) {
      Left(s"`$directory` is not a directory")
    } else {
      val results =
        directory
          .listFiles()
          .sortBy(_.getName())
          .filter(isOasFile)
          .map(file => {
            Thread.sleep(10L)
            publishFile(file.getPath(), useFilenameAsPublisherReference)
          })

      val lefts = results.collect({ case Left(l) => l })

      val rights = results.collect({ case Right(l) => l })

      println(s"Successfully published ${rights.length} APIs")
      if (lefts.nonEmpty) {
        println(s"Failed to publish ${lefts.length} APIs")
      }

      if (lefts.isEmpty) Right(())
      else Left(lefts.mkString("\n"))
    }
  }

  def publishFile(pathname: String, useFilenameAsPublisherReference: Boolean): Either[String, Unit] = {

    println(s"Publishing ${pathname}")

    val file = new File(pathname)
    val filename = file.getName()

    val oasContentBytes = Files.readAllBytes(Paths.get(pathname))
    publish(useFilenameAsPublisherReference = useFilenameAsPublisherReference, filename, oasContentBytes)
  }

  private def publish(useFilenameAsPublisherReference: Boolean, filename: String, oasContent: Array[Byte]): Either[String, Unit] = {

    def getOptionalHeaders() : Map[String, String] = {
      if (useFilenameAsPublisherReference) Map("x-publisher-reference" -> filenameWithoutExtension(filename)) 
      else Map.empty
    }

    val headers = Map("x-specification-type" -> "OAS_V3") ++ getOptionalHeaders()

    val responseEither = publisherConnector.publishApi(headers, filename, oasContent);

    responseEither.flatMap(response => {
      response.statusCode match {
        case 200 | 201       => {
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

  def getApiPublishPhrase(statusCode: Int): String = statusCode match {
    case 200 => "Updated"
    case 201 => "Created"
    case _   => "???"
  }

  private def filenameWithoutExtension(filename: String) : String = {
    filename.replaceFirst("[.][^.]+$", "");
  }
}
