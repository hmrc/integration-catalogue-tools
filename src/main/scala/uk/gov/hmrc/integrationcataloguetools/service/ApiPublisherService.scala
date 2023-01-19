/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.integrationcataloguetools.service

import uk.gov.hmrc.integrationcataloguetools.connectors.PublisherConnector

import java.io.File
import java.nio.file.{Files, Paths}

class ApiPublisherService(publisherConnector: PublisherConnector) extends PublishHelper {



  def publishDirectory(directoryPath: String, useFilenameAsPublisherReference: Boolean): Either[String, Unit] = {

    def isOasFile(file: File): Boolean = {
      if (file.isDirectory) false
      else file.getName.endsWith(".yaml") || file.getName.endsWith(".json")
    }

    val directory = new File(directoryPath)
    if (!directory.isDirectory) {
      Left(s"`$directory` is not a directory")
    } else {
      val results =
        directory
          .listFiles()
          .filter(isOasFile)
          .sortBy(_.getName())
          .map(file => {
            Thread.sleep(10L)
            publishFile(file.getPath, useFilenameAsPublisherReference)
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
    val filename = file.getName

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
    handlePublishResponse(responseEither, filename)
  }



  private def filenameWithoutExtension(filename: String) : String = {
    filename.replaceFirst("[.][^.]+$", "");
  }
}
