package uk.gov.hmrc.integrationcataloguetools

import org.apache.http.impl.client.HttpClients
import uk.gov.hmrc.integrationcataloguetools.models.Platform
import uk.gov.hmrc.integrationcataloguetools.connectors.PublisherConnector
import uk.gov.hmrc.integrationcataloguetools.service.ApiPublisherService

class IntegrationCatalogueTools {

 def printUsage() = {
     println("""
        Usage:
            integration-catalogue-tools --version | -v
            integration-catalogue-tools --help | -h
            integration-catalogue-tools --platform <platform> --csvToOas <inputCsv> <output directory>
            integration-catalogue-tools --csvToFileTransferJson <inputCsv> <output directory>
            integration-catalogue-tools --publishFileTransfers --directory <directory> --url <publish url>
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

  def runApplication(args  : List[String]) = {
       val client = HttpClients.createDefault()
 try {
  args match {
    case Nil | "--help" :: Nil | "-h" :: Nil => {
      printUsage()
      Right()
    }
    case "--version" :: Nil | "-v" :: Nil => {
      printVersion()
      Right()
    }
    case "--platform" :: platform :: "--csvToOas" :: inputCsvFile :: outputPath :: Nil => {
      println(s"Exporting CSV to OAS Files:\nInput file: ${inputCsvFile}\noutput path: ${outputPath}")
      val rowsProcessed = ProcessCsvFile.processApiCsv(inputCsvFile, outputPath, Platform(platform))
      println(s"Exported $rowsProcessed OAS files to:\n${outputPath}")
      Right()
    }
    case "--csvToFileTransferJson" :: inputCsvFile :: outputPath :: Nil => {
      println(s"Exporting CSV to FT Json Files:\nInput file: ${inputCsvFile}\noutput path: ${outputPath}")
      val rowsProcessed = ProcessCsvFile.processFTCsv(inputCsvFile, outputPath)
      println(s"Exported $rowsProcessed FT Json files to:\n${outputPath}")
      Right()
    }

    case "--publish" :: "--platform" :: platform :: "--filename" :: oasFilepath :: "--url" :: publishUrl :: "--authorizationKey" :: authorizationKey :: Nil => {
      val publisher = new ApiPublisherService(new PublisherConnector(publishUrl, client, authorizationKey));
      publisher.publishFile(Platform(platform), oasFilepath)
    }
    case "--publish" :: "--platform" :: platform :: "--directory" :: oasDirectory :: "--url" :: publishUrl :: "--authorizationKey" :: authorizationKey :: Nil => {
      val publisher = new ApiPublisherService(new PublisherConnector(publishUrl, client, authorizationKey));
      publisher.publishDirectory(Platform(platform), oasDirectory)
    }

    case "--publishFileTransfers" :: "--directory" :: ftDirectory :: "--url" :: publishUrl :: "--authorizationKey" :: authorizationKey :: Nil => {
      val publisher = new FileTransferPublisherService(new PublisherConnector(publishUrl, client, authorizationKey));
      publisher.publishDirectory(ftDirectory)
    }
    case options => Left(s"Invalid, unknown or mismatched options or arguments : ${options}\nArgs:${args.toList}")
  }
} finally {
      client.close()
    }
  }
}
