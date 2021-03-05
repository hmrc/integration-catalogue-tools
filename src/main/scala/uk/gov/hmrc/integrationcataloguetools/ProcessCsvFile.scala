package uk.gov.hmrc.integrationcataloguetools

import io.swagger.v3.oas.models.info.{Contact, Info}
import io.swagger.v3.oas.models.media.{Content, MediaType}
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem, Paths}
import io.swagger.v3.oas.models.responses.{ApiResponses, ApiResponse}

import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.JavaConverters._
import java.util.HashMap
import java.io.FileReader

import io.swagger.v3.core.util.Yaml

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.Reader

object ProcessCsvFile {
  private def writeToFile(filename: String, content: String): Unit = {
    import java.io.File
    import java.io.BufferedWriter
    import java.io.FileWriter

    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
  }

  def processApiCsv(inputFilename: String, outputFolder: String) : Int = {
    val in = new FileReader(inputFilename);
    try{
      GenerateOpenApi
        .fromCsvToOasContent(in)
        .map { case (publisherReference, oasContent) => {
          val filename = s"$outputFolder/${publisherReference.value}.yaml"
          writeToFile(filename, oasContent)
        }}.length
      } finally {
        in.close()
      }      
  }

    def processFTCsv(inputFilename: String, outputFolder: String) : Int = {
    val in = new FileReader(inputFilename);
    try{
      GenerateFileTransferJson
        .fromCsvToFileTranferJson(in)
        .map { case (publisherReference, fileTransferJson) => {
          val filename = s"$outputFolder/${publisherReference.value}.json"
          writeToFile(filename, fileTransferJson)
        }}.length
      } finally {
        in.close()
      }      
  }
}
