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

  def publish(filename: String) = {

    val oasContentBytes = Files.readAllBytes(Paths.get(filename))

    println(new String(oasContentBytes, StandardCharsets.UTF_8))
    
    val url = "http://localhost:11114/integration-catalogue-admin-frontend/services/apis/publish"
    
    doPut(url, oasContentBytes);

    def doPut(url: String, oasContent: Array[Byte]) = {
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
        val publisherReference = "1142"
        val filename = "not-really-used"

        put.addHeader("x-platform-type", platformType)
        put.addHeader("x-specification-type", "OAS_V3")
        put.addHeader("x-publisher-reference", publisherReference)
      
        val entity = MultipartEntityBuilder.create()
        entity.addBinaryBody("selectedFile", oasContent, ContentType.DEFAULT_TEXT, filename)
        put.setEntity(entity.build());

        val response = client.execute(put)

        val statusCode = response.getStatusLine().getStatusCode()

        println(s"StatusCode: ${statusCode}")

        val contentStream = response.getEntity().getContent()
        val content = scala.io.Source.fromInputStream(contentStream).mkString
        
        println(s"Content: ${content}")
      } finally {
        client.close()
      }
    }

      // --header 'x-platform-type: '$plat \
      // --header 'x-specification-type: OAS_V3' \
      // --header 'x-publisher-reference: '$reference \

      // val postData = Map("fields" -> "data")

      // wsClient
      //   .url("http://localhost:11114/integration-catalogue-admin-frontend/services/apis/publish")
      //   .post(postData)
      //   .map { response =>
      //     val statusText: String = response.statusText
      //     val body = response.body[String]
      //     println(s"Got a response $statusText: $body")
        // }
    
  }
}

