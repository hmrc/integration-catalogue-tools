package uk.gov.hmrc.integrationcataloguetools

import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.charset.StandardCharsets

import akka.actor.ActorSystem
import akka.stream._
import play.api.libs.ws._
import play.api.libs.ws.ahc._

import scala.concurrent.Future

import scala.concurrent.Future
import scala.concurrent.duration._
// import play.api.mvc._
import play.api.libs.ws._
// import play.api.http.HttpEntity
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString

import akka.stream.Materializer

import scala.concurrent.ExecutionContext

// import play.api.libs.ws.WSBodyWritables._
import play.api.libs.ws.DefaultBodyReadables._
import play.api.libs.ws.DefaultBodyWritables._


// From
// https://github.com/playframework/play-ws

object Publisher {
  import DefaultBodyReadables._
  import scala.concurrent.ExecutionContext.Implicits._

  def publish(filename: String) = {

    val oasContentBytes = Files.readAllBytes(Paths.get(filename))

    println(new String(oasContentBytes, StandardCharsets.UTF_8))

    implicit val system = ActorSystem()
    // system.registerOnTermination {
    //   System.exit(0)
    // }
    
    implicit val materializer = SystemMaterializer(system).materializer

    val wsClient = StandaloneAhcWSClient()

    publishOas(wsClient, oasContentBytes)
      .andThen { case _ => wsClient.close() }
      .andThen { case _ => system.terminate() }

    // call(wsClient)
      // .andThen { case _ => wsClient.close() }
      // .andThen { case _ => system.terminate() }

    // def call(wsClient: StandaloneWSClient): Future[Unit] = {
    //   wsClient.url("https://jsonplaceholder.typicode.com/todos/1").get().map { response =>
    //     val statusText: String = response.statusText
    //     val body = response.body[String]
    //     println(s"Got a response $statusText: $body")
    //   }
    // }

    def publishOas(wsClient: StandaloneWSClient, oasContentBytes: Seq[Byte]): Future[Unit] = {

      // val source = Source(play.api.mvc.MultipartFormData.FilePart.FilePart("hello", "hello.txt", Option("text/plain"), oasContentBytes))

      // --header 'x-platform-type: '$plat \
      // --header 'x-specification-type: OAS_V3' \
      // --header 'x-publisher-reference: '$reference \

      val postData = Map("fields" -> "data")

      wsClient
        .url("http://localhost:11114/integration-catalogue-admin-frontend/services/apis/publish")
        .post(postData)
        .map { response =>
          val statusText: String = response.statusText
          val body = response.body[String]
          println(s"Got a response $statusText: $body")
        }
    }
  }
}

