/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  def publishDirectory(directoryPath: String) : Either[String, Unit]= {
    
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
          .map(file => publishFile(file.getPath()) )

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

  def publishFile(pathname: String) : Either[String, Unit] = {

    println(s"Publishing ${pathname}")

    val file = new File(pathname)
    val filename = file.getName()

    val fileContents = Files.readAllLines(Paths.get(pathname)).asScala.mkString

    publish(publisherReference = PublisherReference(filename), filename, fileContents)
  }

  private def publish(publisherReference: PublisherReference, filename: String, jsonString: String) : Either[String, Unit] = {
      val responseEither = publisherConnector.publishFileTransfer(jsonString);

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

