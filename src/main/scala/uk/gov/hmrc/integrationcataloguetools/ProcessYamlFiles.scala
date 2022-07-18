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

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Files
import java.time.Instant
import java.util.regex.Pattern
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
              writeOutputFile(
                s"$outputPath/${file.getName}",
                insertXIntegrationCatalogue(
                  new String(Files.readAllBytes(file.toPath)),
                  platform,
                  reviewedDate,
                  file.getName.extractPublisherReference
                )
              )
            }.length
        }
    }
  }

  // Handle Windows, MacOS and Linux line endings
  private val newLineRegex = "(\\r\\n|\\r|\\n)"
  // This regex finds the 'info' element so that the x-integration-catalogue can be added at the end of it
  // The \X at the start will detect any Byte Order Marks, e.g., the UTF-8 BOM, found in some YAML files
  private val findInfoRegex = Pattern.compile(s"((\\X*$newLineRegex)*info:($newLineRegex .*)*)")

  def insertXIntegrationCatalogue(fileContents: String, platform: String, reviewedDate: String, publisherReference: String): String = {
    val xIntegrationCatalogue = "x-integration-catalogue:"
    if (fileContents.contains(xIntegrationCatalogue)) fileContents
    else
      findInfoRegex.matcher(fileContents).replaceFirst(
        s"""$$1
           |  $xIntegrationCatalogue
           |    reviewed-date: $reviewedDate
           |    platform: $platform
           |    publisher-reference: $publisherReference""".stripMargin
      )
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

  private def writeOutputFile(outputFileName: String, contents: String) = {
    val writer = new BufferedWriter(new FileWriter(new File(outputFileName)))
    writer.write(contents)
    writer.close()
  }

}
