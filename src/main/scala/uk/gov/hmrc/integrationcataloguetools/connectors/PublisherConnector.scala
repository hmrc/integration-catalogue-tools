package uk.gov.hmrc.integrationcataloguetools.connectors

import scala.util.Try
import scala.util.Success
import scala.util.Failure

case class Response(statusCode: Int, content: String)

class PublisherConnector(url: String) {

  def publish(headers : Map[String, String], filename: String, oasContent: Array[Byte]) : Either[String, Response] = {
      import org.apache.http.entity.ContentType
      import org.apache.http.entity.mime.MultipartEntityBuilder
      import org.apache.http.impl.client.HttpClients
      import org.apache.http.Header
      import org.apache.http.client.methods.HttpPut

      val client = HttpClients.createDefault()
      try {
        var put = new HttpPut(url)
        headers.foreach(header => put.addHeader(header._1, header._2))
        
        val entity = MultipartEntityBuilder.create()
        entity.addBinaryBody("selectedFile", oasContent, ContentType.DEFAULT_TEXT, filename)
        put.setEntity(entity.build());
        
          Try(client.execute(put))
            .map(response => {
              val content = scala.io.Source.fromInputStream(response.getEntity().getContent()).mkString
              Response(response.getStatusLine().getStatusCode(), content)
            }) match { 
              case Success(response) => Right(response)
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
