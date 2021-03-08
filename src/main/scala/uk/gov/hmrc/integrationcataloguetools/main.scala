package uk.gov.hmrc.integrationcataloguetools

import java.io.FileReader

import uk.gov.hmrc.integrationcataloguetools.connectors.PublisherConnector

import uk.gov.hmrc.integrationcataloguetools.models._
import org.apache.http.impl.client.HttpClients
object Main extends App {

  def printUsage() = {
     println("""
  Usage:
    integration-catalogue-tools --version | -v
    integration-catalogue-tools --help | -h
    integration-catalogue-tools --csvToOas <inputCsv> <output directory>
    integration-catalogue-tools --csvToFileTransferJson <inputCsv> <output directory>
    integration-catalogue-tools --publishFileTransfer --platform <platform> --filename <filetransferCsv> --url <publish url>
    integration-catalogue-tools --publish --platform <platform> --filename <oasFile> --url <publish url>
    integration-catalogue-tools --publish --platform <platform> --directory <directory> --url <publish url>
      Arguments:
        - directory : All files with .yaml or .json extension will be procesed
""")
  }

  def printVersion() = {
    val title = getClass().getPackage().getImplementationTitle()
    val version =getClass().getPackage().getImplementationVersion()

    println(s"integration-catalogue-tools version '${version}'")
  }
  val client = HttpClients.createDefault()
  val result : Either[String, Unit] = args.toList match {
    case Nil | "--help" :: Nil | "-h" :: Nil => {
      printUsage()
      Right()
    }
    case "--verion" :: Nil | "-v" :: Nil => {
      printVersion()
      Right()
    }
    case "--csvToOas" :: inputCsvFile :: outputPath :: Nil => {
      println(s"Exporting CSV to OAS Files:\nInput file: ${inputCsvFile}\noutput path: ${outputPath}")
      val rowsProcessed = ProcessCsvFile.processApiCsv(inputCsvFile, outputPath)
      println(s"Exported $rowsProcessed OAS files to:\n${outputPath}")
      Right()
    }
    case "--csvToFileTransferJson" :: inputCsvFile :: outputPath :: Nil => {
      println(s"Exporting CSV to FT Json Files:\nInput file: ${inputCsvFile}\noutput path: ${outputPath}")
      val rowsProcessed = ProcessCsvFile.processFTCsv(inputCsvFile, outputPath)
      println(s"Exported $rowsProcessed FT Json files to:\n${outputPath}")
      Right()
    }

    case "--publish" :: "--platform" :: platform :: "--filename" :: oasFilepath :: "--url" :: publishUrl :: Nil => {
      val publisher = new ApiPublisherService(new PublisherConnector(publishUrl, client));
      publisher.publishFile(Platform(platform), oasFilepath)
    }
    case "--publish" :: "--platform" :: platform :: "--directory" :: oasDirectory :: "--url" :: publishUrl :: Nil => {
      val publisher = new ApiPublisherService(new PublisherConnector(publishUrl, client));
      publisher.publishDirectory(Platform(platform), oasDirectory)
    }

    case "--publishFileTransfers" :: "--directory" :: ftDirectory :: "--url" :: publishUrl :: Nil => {
      val publisher = new FileTransferPublisherService(new PublisherConnector(publishUrl, client));
      publisher.publishDirectory(ftDirectory)
    }
    case options => Left(s"Invalid, unknown or mismatched options or arguments : ${options}\nArgs:${args.toList}")
  }

  result match{
    case Left(error) => {
      println(s"Error: $error")
      java.lang.System.exit(1)
    }
    case Right(_) => ();
  }
}
