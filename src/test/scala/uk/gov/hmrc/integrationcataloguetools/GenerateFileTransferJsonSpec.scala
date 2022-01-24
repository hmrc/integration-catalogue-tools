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

import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.integrationcataloguetools.models.FileTransferPublishRequest

import scala.io.Source

class GenerateFileTransferJsonSpec extends AnyWordSpec with Matchers {
  "Parse CSV into File Transfer Json list" in {
    val csvFile = Source.fromResource("generatefiletransferjsonspec/FileTransferDataCsv.csv")
    
    val fileTransfers : Seq[(_, FileTransferPublishRequest)] = GenerateFileTransferJson.fromCsvToFileTransferJson(csvFile.bufferedReader())

    fileTransfers should have length 1

    val (publisherReference, fileTransfer) = fileTransfers.head

    fileTransfer.contact.name shouldBe "EIS Front Door"
    fileTransfer.contact.emailAddress shouldBe "services.enterpriseintegration@hmrc.gov.uk"
    fileTransfer.description shouldBe "A file transfer from BMC to Service Now"
    fileTransfer.platformType shouldBe "CORE_IF_FILE_TRANSFER_FLOW"
    fileTransfer.title shouldBe "BMC-ServiceNow-NetworksDataDaily-notify"
    fileTransfer.publisherReference.value shouldBe "BMC-ServiceNow-NetworksDataDaily-notify"
    fileTransfer.fileTransferPattern shouldBe "Corporate to corporate"
    fileTransfer.sourceSystem.head shouldBe "BMC"
    fileTransfer.targetSystem.head shouldBe "ServiceNow"

  }
implicit val formats = DefaultFormats


def runTest(csvName: String, expectedJsonFileName: String) ={
      val csvFile = Source.fromResource(s"generatefiletransferjsonspec/$csvName")
    val expectedContent = Source.fromResource(s"generatefiletransferjsonspec/$expectedJsonFileName").mkString

    val contents = GenerateFileTransferJson.fromCsvToFileTransferJson(csvFile.bufferedReader())

    contents should have length 1

    val content = write(contents.head._2)

    content shouldBe expectedContent
}


  "Parse CSV into File Transfer Json content with multiple transports" in {
    runTest("FileTransferDataCsv.csv", "BMC-ServiceNow-NetworksDataDaily-notify.json")
  }


  "Parse CSV into File Transfer Json content with single transport" in {
    runTest("FileTransferDataCsv-1transport.csv", "BMC-ServiceNow-NetworksDataDaily-notify-1transport.json")
  }

}
