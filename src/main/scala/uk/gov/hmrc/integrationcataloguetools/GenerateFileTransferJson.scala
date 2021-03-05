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

import uk.gov.hmrc.integrationcataloguetools.models._

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.Reader
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.time.ZoneOffset

object GenerateFileTransferJson {
  
  def fromCsvToFileTranferJson(reader: Reader): Seq[(PublisherReference, FileTransferPublishRequest)] = {

    def createFileTransferPublishRequest(record: CSVRecord): FileTransferPublishRequest = {
       val expectedValues = 9
      if (record.size() < expectedValues) throw new RuntimeException(s"Expected $expectedValues values on row ${record.getRecordNumber()}")

      def parseString(s: String): String = {
        Option(s).getOrElse("").trim()
      }

      // "2020-11-04T20:27:05.000Z"

      // val dateValue: DateTime = DateTime.parse("04/11/2020 20:27:05", DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss"));
      //TODO: do we validate the date format / validity of the date string here?
      //val date = ZonedDateTime.now()
      //val formattedString2: String = date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      // PublisherReference, title, description, platform, contactName, ContactEmail, sourceSystem, targetSystem, fileTransferPattern
      FileTransferPublishRequest(
        publisherReference = PublisherReference(parseString(record.get("PublisherReference"))),
        title = parseString(record.get("Title")),
        description = parseString(record.get("Description")),
        lastUpdated =  parseString(record.get("LastUpdated")),
        platformType = parseString(record.get("Platform")),
        contact = ContactInformation(parseString(record.get("ContactName")), parseString(record.get("ContactEmail"))),
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
