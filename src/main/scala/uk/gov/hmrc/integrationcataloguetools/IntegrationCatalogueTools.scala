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

package uk.gov.hmrc.integrationcataloguetools

import org.apache.http.impl.client.HttpClients

import uk.gov.hmrc.integrationcataloguetools.connectors.PublisherConnector
import uk.gov.hmrc.integrationcataloguetools.models.Platform
import uk.gov.hmrc.integrationcataloguetools.service.{ApiPublisherService, FileTransferPublisherService}

class IntegrationCatalogueTools {

  def printUsage(): Unit = {
    println("""
        Usage:
            integration-catalogue-tools --version | -v
            integration-catalogue-tools --help | -h
            integration-catalogue-tools --csvToOas <inputCsv> <output directory>
            integration-catalogue-tools --csvToFileTransferJson <inputCsv> <output directory>
            integration-catalogue-tools --yamlFindApisToUnpublish <previous directory> <updated directory>
            integration-catalogue-tools --yamlAddMetadata <input directory> <platform> <reviewed date> <output directory>
            integration-catalogue-tools --publish [--useFilenameAsPublisherReference] --platform <platform> --filename <oasFile> --url <publish url> --authorizationKey <key>
            integration-catalogue-tools --publish [--useFilenameAsPublisherReference] --platform <platform> --directory <directory> --url <publish url> --authorizationKey <key>
            integration-catalogue-tools --publishFileTransfers --platform <platform> --directory  <directory> --url <publish url> --authorizationKey <key>
            
            Arguments:
                - directory : All files with .yaml or .json extension will be processed
                - useFilenameAsPublisherReference : Uses the filename as the optional publisherReference header. If not included the publisherReference must be present in the OpenAPI Specification file
        """)
  }

  def printVersion(): Unit = {
    // val title = getClass.getPackage.getImplementationTitle
    val version = getClass.getPackage.getImplementationVersion

    println(s"integration-catalogue-tools version '$version'")
  }

  def runApplication(args: List[String]): Either[String, Unit] = {
    val client = HttpClients.createDefault()
    try {
      args match {
        case Nil | "--help" :: Nil | "-h" :: Nil                            =>
          printUsage()
          Right(())
        case "--version" :: Nil | "-v" :: Nil                               =>
          printVersion()
          Right(())
        case "--csvToOas" :: inputCsvFile :: outputPath :: Nil              =>
          println(s"Exporting CSV to OAS Files:\nInput file: $inputCsvFile\noutput path: $outputPath")
          val rowsProcessed = ProcessCsvFile.processApiCsv(inputCsvFile, outputPath)
          println(s"Exported $rowsProcessed OAS files to:\n$outputPath")
          Right(())
        case "--csvToFileTransferJson" :: inputCsvFile :: outputPath :: Nil =>
          println(s"Exporting CSV to FT Json Files:\nInput file: $inputCsvFile\noutput path: $outputPath")
          val rowsProcessed = ProcessCsvFile.processFTCsv(inputCsvFile, outputPath)
          println(s"Exported $rowsProcessed FT Json files to:\n$outputPath")
          Right(())

        case "--yamlFindApisToUnpublish" :: previousPath :: updatedPath :: Nil =>
          println(s"Finding APIs to Unpublish from ${substringAfterLastSlash(previousPath)}:\nPrevious path: $previousPath\nUpdated path: $updatedPath")
          CompareYamlFiles.findApisToUnpublish(previousPath, updatedPath) match {
            case Right(apisToUnpublish) =>
              println(s"APIs to unpublish: \n ${apisToUnpublish.mkString(" \n")}")
              Right(())
            case Left(errorMessage)     => Left(errorMessage)
          }

        case "--yamlAddMetadata" :: inputPath :: platform :: reviewedDate :: outputPath :: Nil =>
          println(s"Adding Metadata to YAML Files:\nInput path: $inputPath\nPlatform: $platform\nReviewed date: $reviewedDate\nOutput path: $outputPath")
          ProcessYamlFiles.addMetadata(inputPath, platform, reviewedDate, outputPath) match {
            case Right(filesProcessed: Int) =>
              println(s"Processed $filesProcessed files")
              Right(())
            case Left(errorMessage: String) => Left(errorMessage)
          }

        case "--publish" :: "--platform" :: platform :: "--filename" :: oasFilepath :: "--url" :: publishUrl :: "--authorizationKey" :: authorizationKey :: Nil =>
          val publisher = new ApiPublisherService(new PublisherConnector(publishUrl, client, Platform(platform), authorizationKey))
          publisher.publishFile(oasFilepath, useFilenameAsPublisherReference = false)

        case "--publish" :: "--useFilenameAsPublisherReference" :: "--platform" :: platform :: "--filename" :: oasFilepath :: "--url" :: publishUrl :: "--authorizationKey" :: authorizationKey :: Nil =>
          val publisher = new ApiPublisherService(new PublisherConnector(publishUrl, client, Platform(platform), authorizationKey))
          publisher.publishFile(oasFilepath, useFilenameAsPublisherReference = true)

        case "--publish" :: "--platform" :: platform :: "--directory" :: oasDirectory :: "--url" :: publishUrl :: "--authorizationKey" :: authorizationKey :: Nil =>
          val publisher = new ApiPublisherService(new PublisherConnector(publishUrl, client, Platform(platform), authorizationKey))
          publisher.publishDirectory(oasDirectory, useFilenameAsPublisherReference = false)

        case "--publish" :: "--useFilenameAsPublisherReference" :: "--platform" :: platform :: "--directory" :: oasDirectory :: "--url" :: publishUrl :: "--authorizationKey" :: authorizationKey :: Nil =>
          val publisher = new ApiPublisherService(new PublisherConnector(publishUrl, client, Platform(platform), authorizationKey))
          publisher.publishDirectory(oasDirectory, useFilenameAsPublisherReference = true)

        case "--publishFileTransfers" :: "--platform" :: platform :: "--directory" :: ftDirectory :: "--url" :: publishUrl :: "--authorizationKey" :: authorizationKey :: Nil =>
          val publisher = new FileTransferPublisherService(new PublisherConnector(publishUrl, client, Platform(platform), authorizationKey))
          publisher.publishDirectory(ftDirectory)

        case options => Left(s"Invalid, unknown or mismatched options or arguments : $options\nArgs:$args")
      }
    } finally {
      client.close()
    }
  }

  private def substringAfterLastSlash(s: String) = s.reverse.takeWhile(_ != '/').reverse.toUpperCase

}
