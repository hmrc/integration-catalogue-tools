package uk.gov.hmrc.integrationcataloguetools

import java.io.FileReader

object Main extends App {

  def printUsage() = {
     println("""
  Usage:
    integration-catalogue-tools --version | -v
    integration-catalogue-tools --help | -h
    integration-catalogue-tools --csvToOas <input CSV> <output directory>
    integration-catalogue-tools --publish --platform <platform> --file <oas file>
    integration-catalogue-tools --publish --platform <platform> --directory <directory>
""")
  }

  def printVersion() = {
    val title = getClass().getPackage().getImplementationTitle()
    val version =getClass().getPackage().getImplementationVersion()

    println(s"integration-catalogue-tools version '${version}'")
  }
  
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
      val rowsProcessed = ProcessCsvFile.process(inputCsvFile, outputPath)
      println(s"Exported $rowsProcessed OAS files to:\n${outputPath}")
      Right()
    }
    case "--publish" :: "--platform" :: platform :: "--file" :: oasFilepath :: Nil => {
      Publisher.publishFile(platform, oasFilepath)
    }
    case "--publish" :: "--platform" :: platform :: "--directory" :: oasDirectory :: Nil => {
      Publisher.publishDirectory(platform, oasDirectory)
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
