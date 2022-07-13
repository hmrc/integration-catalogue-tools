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

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Files
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

object ProcessYamlFiles {

  def addMetadata(inputPath: String, platform: String, reviewedDate: String, outputPath: String): Long = {
    if (inputsAreNotValid(inputPath, reviewedDate, outputPath)) {
      0
    } else {
      val outputDirectory = new File(outputPath)
      outputDirectory.mkdirs()

      val inputDirectory = new File(inputPath)
      inputDirectory.listFiles
        .filter(_.isFile)
        .filter(_.getName.endsWith(".yaml"))
        .map { file =>
          writeOutputFile(
            s"$outputPath/${file.getName}",
            insertXIntegrationCatalogue(
              new String(Files.readAllBytes(file.toPath)),
              platform,
              reviewedDate,
              extractPublisherReference(file.getName)
            )
          )
        }
        .length
    }
  }

  private val publisherReferenceRegex = Pattern.compile("api.*?(\\d{4})", Pattern.CASE_INSENSITIVE)

  def extractPublisherReference(fileName: String): String = {
    val matcher = publisherReferenceRegex.matcher(fileName)
    if (matcher.find()) matcher.group(1) else ""
  }

  private val findInfoRegex = "((.*\\n)*info:(\\n +.*)*)"

  def insertXIntegrationCatalogue(fileContents: String, platform: String, reviewedDate: String, publisherReference: String): String = {
    val xIntegrationCatalogueTemplate =
      s"""$$1
         |  x-integration-catalogue:
         |    reviewed-date: $reviewedDate
         |    platform: $platform
         |    publisher-reference: $publisherReference""".stripMargin

    fileContents.replaceFirst(findInfoRegex, xIntegrationCatalogueTemplate)
  }

  private def inputsAreNotValid(inputPath: String, reviewedDate: String, outputPath: String): Boolean = {
    var result = false
    val inputDirectory = new File(inputPath)
    if (!inputDirectory.exists || !inputDirectory.isDirectory) {
      println("Input path is not a directory")
      result = true
    }
    val outputDirectory = new File(outputPath)

    if (outputDirectory.exists) {
      println("Output path is not empty")
      result = true
    }
    val reformattedReviewedData = ZonedDateTime.parse(reviewedDate).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
    if (reviewedDate.trim.isEmpty || reviewedDate != reformattedReviewedData) {
      println("Reviewed date is not in ISO-8601 format")
      result = true
    }
    result
  }

  private def writeOutputFile(outputFileName: String, output: String) = {
    val writer = new BufferedWriter(new FileWriter(new File(outputFileName)))
    writer.write(output)
    writer.close()
  }

}
