package uk.gov.hmrc.integrationcataloguetools

import io.swagger.v3.oas.models.info.{Contact, Info}
import io.swagger.v3.oas.models.media.{Content, MediaType}
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem, Paths}
import io.swagger.v3.oas.models.responses.{ApiResponse, ApiResponses}

import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.JavaConverters._
import java.util.HashMap
import java.io.FileReader

import io.swagger.v3.core.util.Yaml

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.Reader
import java.time.LocalDateTime

case class ContactInformation(name: String, emailAddress: String)

case class FileTransferPublishRequest(
    publisherReference: PublisherReference,
    fileTransferSpecificationVersion: String = "0.1",
    title: String,
    description: String,
    platform: String,
    lastUpdated: LocalDateTime = LocalDateTime.now(),
    contactInfo: ContactInformation,
    sourceSystem: List[String], // One or many
    targetSystem: List[String],
    fileTransferPattern: String)

object GenerateFileTransferJson {
  
  def fromCsvToFileTranferJson(reader: Reader): Seq[(PublisherReference, FileTransferPublishRequest)] = {

    def createFileTransferPublishRequest(record: CSVRecord): FileTransferPublishRequest = {
       val expectedValues = 9
      if (record.size() < expectedValues) throw new RuntimeException(s"Expected $expectedValues values on row ${record.getRecordNumber()}")

      def parseString(s: String): String = {
        Option(s).getOrElse("").trim()
      }
      // PublisherReference, title, description, platform, contactName, ContactEmail, sourceSystem, targetSystem, fileTransferPattern
      FileTransferPublishRequest(
        publisherReference = PublisherReference(parseString(record.get("PublisherReference"))),
        title = parseString(record.get("Title")),
        description = parseString(record.get("Description")),
        platform = parseString(record.get("Platform")),
        contactInfo = ContactInformation(parseString(record.get("ContactName")), parseString(record.get("ContactEmail"))),
        sourceSystem = List(parseString(record.get("Source"))),
        targetSystem = List(parseString(record.get("Target"))),
        fileTransferPattern = parseString(record.get("Pattern"))
      )
    }


    
    org.apache.commons.csv.CSVFormat.EXCEL
      // .withHeader("PublisherReference", "Title", "Description", "Platform", "ContactName", "ContactEmail", "Source", "Target", "Pattern")
      .withFirstRecordAsHeader()
      .parse(reader).getRecords().asScala.toSeq
      .map(createFileTransferPublishRequest)
      .map(x=> (x.publisherReference, x))

  }
}
