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
import org.apache.http.client.methods.HttpPut
import scala.util.Try
import scala.util.Success
import scala.util.Failure


case class PublisherReference(value: String) extends AnyVal
case class Platform(value: String) extends AnyVal

object Publisher {
  import scala.concurrent.ExecutionContext.Implicits._

  def publishDirectory(platform: Platform, directoryPath: String, publishUrl: String) : Either[String, Unit]= {
    
    var directory = new File(directoryPath)
    if (!directory.isDirectory()){
      Left(s"$directory is not a directory")
    } else {
      val results = 
        directory
          .listFiles()
          .map(file => publishFile(platform, file.getPath() , publishUrl) )

      val lefts = results.collect({ case Left(l) => l})

      if (lefts.isEmpty) Right(())
      else Left(lefts.mkString("\n"))
    }
  }

  def publishFile(platform: Platform, pathname: String, publishUrl: String) : Either[String, Unit] = {

    println(s"Publishing ${pathname}")

    val file = new File(pathname);
    val filename = file.getName()

    val oasContentBytes = Files.readAllBytes(Paths.get(pathname))    

    doPut(platform,  publisherReference = PublisherReference(filename), filename, publishUrl, oasContentBytes);
  }

  private def doPut(platform: Platform, publisherReference: PublisherReference, filename: String, url: String, oasContent: Array[Byte]) : Either[String, Unit] = {
      
      import org.apache.http.entity.ContentType
      import org.apache.http.entity.mime.MultipartEntityBuilder
      import org.apache.http.impl.client.HttpClients

      val client = HttpClients.createDefault()
      try {
        var put = new HttpPut(url)
        put.addHeader("x-platform-type", platform.value)
        put.addHeader("x-specification-type", "OAS_V3")
        put.addHeader("x-publisher-reference", publisherReference.value)
      
        val entity = MultipartEntityBuilder.create()
        entity.addBinaryBody("selectedFile", oasContent, ContentType.DEFAULT_TEXT, filename)
        put.setEntity(entity.build());
        
          Try(client.execute(put))
            .map(response => {
              val statusCode = response.getStatusLine().getStatusCode()
              val contentStream = response.getEntity().getContent()
              val content = scala.io.Source.fromInputStream(contentStream).mkString

              statusCode match {
                case 200 => {
                  println(s"Published. Updated API. Response(${statusCode}): ${content}") 
                  Right(())
                }
                case 201 => {
                  println(s"Published. Created API. Response(${statusCode}): ${content}") 
                  Right(())
                }
                case otherStatusCode => {
                  val errorMessage = s"Failed to publish '$filename'. Response(${statusCode}): ${content}"
                  Left(errorMessage)
                }
              }
            }) match { 
              case Success(either) => either
              case Failure(exception) => {
                println("Error calling publish service:")
                exception.printStackTrace()
                Left(exception.getMessage())
              }
            }
      } finally {
        client.close()
      }
    }
}

