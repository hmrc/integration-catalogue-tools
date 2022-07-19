/*
 * Copyright 2022 HM Revenue & Customs
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

import uk.gov.hmrc.integrationcataloguetools.utils.ExtractPublisherReference.Implicits

import java.io.{File, PrintWriter}
import java.time.Instant
import scala.io.Source
import scala.util.{Failure, Success, Try}

object ProcessYamlFiles {

  def addMetadata(inputPath: String, platform: String, reviewedDate: String, outputPath: String): Either[String, Int] = {
    validateInputs(inputPath, reviewedDate, outputPath) match {
      case Some(errorMessage) => Left(errorMessage)
      case None               => Right {

          new File(outputPath).mkdirs()

          new File(inputPath)
            .listFiles
            .filter(_.isFile)
            .filter(_.getName.endsWith(".yaml"))
            .map { file =>
              val source = Source.fromFile(file)
              writeOutputFile(
                new File(s"$outputPath/${file.getName}"),
                insertXIntegrationCatalogue(
                  source.getLines().toList,
                  platform,
                  reviewedDate,
                  file.getName.extractPublisherReference
                )
              )
              source.close()
            }.length
        }
    }
  }

  def insertXIntegrationCatalogue(fileContents: List[String], platform: String, reviewedDate: String, publisherReference: String): List[String] = {
    val xIntegrationCatalogue = "x-integration-catalogue:"
    if (fileContents.exists(_.contains(xIntegrationCatalogue)) || !fileContents.exists(_.startsWith("info:"))) {
      fileContents
    } else {
      val afterInfoSection = fileContents.dropWhile(!_.startsWith("info:")).drop(1).dropWhile(next => next.startsWith(" ") || next.isEmpty)
      val upToInfoSection = fileContents.take(fileContents.size - afterInfoSection.size)

      upToInfoSection ++ List(
        s"  $xIntegrationCatalogue",
        s"    reviewed-date: $reviewedDate",
        s"    platform: $platform",
        s"    publisher-reference: $publisherReference"
      ) ++ afterInfoSection
    }
  }

  private def validateInputs(inputPath: String, reviewedDate: String, outputPath: String): Option[String] = {
    val inputDirectory = new File(inputPath)
    if (!inputDirectory.exists || !inputDirectory.isDirectory) {
      Some("Input path is not a directory")
    } else if (new File(outputPath).exists) {
      Some("Output path is not empty")
    } else {
      validateReviewedDate(reviewedDate)
    }
  }

  def validateReviewedDate(reviewedDate: String): Option[String] = {
    Try {
      Instant.parse(reviewedDate)
    } match {
      case Success(_) => None
      case Failure(_) => Some("Reviewed date is not in ISO-8601 format")
    }
  }

  private def writeOutputFile(outputFile: File, contents: List[String]): Unit = {
    new PrintWriter(outputFile) {
      contents.foreach(println)
      close()
    }
  }

}
