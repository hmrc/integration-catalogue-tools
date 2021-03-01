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


object Publisher {
  import scala.concurrent.ExecutionContext.Implicits._

  def publishDirectory(directoryPath: String) : Either[String, Unit]= {
    
    var directory = new File(directoryPath)
    if (directory.isDirectory()){
      // println("TODO publishPath: " + directory)
      // println("D: " + directory.listFiles().mkString("\n"))

      directory
        .listFiles()
        .foreach(oasFile => {
          publishFile(oasFile.getPath())
        })
      
      Right( () )
    } else {
      Left(s"$directory is not a directory")
    }
  }

  def publishFile(pathname: String) : Either[String, Unit] = {

    println(s"Publishing ${pathname}")

    val file = new File(pathname);
    val filename = file.getName()

    val oasContentBytes = Files.readAllBytes(Paths.get(pathname))    
    
    val url = "http://localhost:11114/integration-catalogue-admin-frontend/services/apis/publish"
    
    doPut(url, oasContentBytes, publisherReference = filename, filename);

    Right( () )
  }

  private def doPut(url: String, oasContent: Array[Byte], publisherReference: String, filename: String) = {
      import org.apache.http.client.methods.{HttpGet, HttpPost}
      import org.apache.http.entity.ContentType
      import org.apache.http.entity.mime.MultipartEntityBuilder
      import org.apache.http.impl.client.HttpClients
      import org.apache.http.util.EntityUtils

      val client = HttpClients.createDefault()
      try {
        var put = new HttpPut(url)

        // TODO
        val platformType = "DES"

        put.addHeader("x-platform-type", platformType)
        put.addHeader("x-specification-type", "OAS_V3")
        put.addHeader("x-publisher-reference", publisherReference)
      
        val entity = MultipartEntityBuilder.create()
        entity.addBinaryBody("selectedFile", oasContent, ContentType.DEFAULT_TEXT, filename)
        put.setEntity(entity.build());

        val response = client.execute(put)

        val statusCode = response.getStatusLine().getStatusCode()

        val contentStream = response.getEntity().getContent()
        val content = scala.io.Source.fromInputStream(contentStream).mkString

        statusCode match {
          case 200 => println(s"Published. Updated API. Response(${statusCode}): ${content}")
          case 201 => println(s"Published. Created API. Responce(${statusCode}): ${content}")
        }
      } finally {
        client.close()
      }
    }
}

