/*
 * Copyright 2021 HM Revenue & Customs
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

import org.apache.commons.csv.CSVRecord
import uk.gov.hmrc.integrationcataloguetools.models._

import java.io.Reader
import scala.collection.JavaConverters._

object GenerateFileTransferJson {
  
  def fromCsvToFileTransferRequest(reader: Reader): Seq[(PublisherReference, FileTransferPublishRequest)] = {

    def createFileTransferPublishRequest(record: CSVRecord): FileTransferPublishRequest = {
       val expectedValues = 10
      if (record.size() < expectedValues) throw new RuntimeException(s"Expected $expectedValues values on row ${record.getRecordNumber}")

      def parseString(s: String): String = {
        Option(s).getOrElse("").trim()
      }

      def getTransports() :List[String] ={
        val transport1 = parseString(record.get("Transport1"))
        val transport2 = parseString(record.get("Transport2"))

        List(transport1, transport2).filterNot(_.isEmpty)
      }
      // val dateValue: DateTime = DateTime.parse("04/11/2020 20:27:05", DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss"));
      //TODO: do we validate the date format / validity of the date string here?
      //val date = ZonedDateTime.now()
      //val formattedString2: String = date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    
      // PublisherReference, title, description, platform, contactName, ContactEmail, sourceSystem, targetSystem, fileTransferPattern, LastUpdated
      FileTransferPublishRequest(
        publisherReference = PublisherReference(parseString(record.get("PublisherReference"))),
        title = parseString(record.get("Title")),
        description = parseString(record.get("Description")),
        lastUpdated =  parseString(record.get("LastUpdated")),
        reviewedDate =  parseString(record.get("ReviewedDate")),
        platformType = parseString(record.get("Platform")),
        contact = ContactInformation(parseString(record.get("ContactName")), parseString(record.get("ContactEmail"))),
        sourceSystem = List(parseString(record.get("Source"))),
        targetSystem = List(parseString(record.get("Target"))),
        transports = getTransports(),
        fileTransferPattern = parseString(record.get("Pattern"))
      )
    }


    
    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .parse(reader).getRecords.asScala
      .map(createFileTransferPublishRequest)
      .map(x=> (x.publisherReference, x))

  }
}
