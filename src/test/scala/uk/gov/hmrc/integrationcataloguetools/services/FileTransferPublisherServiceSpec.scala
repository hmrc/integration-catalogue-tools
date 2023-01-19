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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import uk.gov.hmrc.integrationcataloguetools.connectors._

import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchersSugar

import uk.gov.hmrc.integrationcataloguetools.service.FileTransferPublisherService

class FileTransferPublisherServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ArgumentMatchersSugar {

  val testResourcesPath = "src/test/resources/publishservicespec/"

  trait Setup {
    val mockPublisherConnector = mock[PublisherConnector]
    val service                = new FileTransferPublisherService(mockPublisherConnector)
  }

  "Publish directory of files" should {
    val filename2 = "BMC-ServiceNow-NetworksDataDaily-notify.json"

    val directoryPath = testResourcesPath + "directory-of-ft-json-files"

    "be sucessfull if publish returns a 2xx" in new Setup {
      when(mockPublisherConnector.publishFileTransfer((*)))
        .thenReturn(Right(Response(200, "")))

      val result = service.publishDirectory(directoryPath)

      result shouldBe Right(())

      val expectedFtJsonContent1 =
        """{"publisherReference":"BMC-ServiceNow-NetworksDataDaily-notify","fileTransferSpecificationVersion":"0.1","title":"BMC-ServiceNow-NetworksDataDaily-notify","description":"A file transfer from BMC to Service Now","platformType":"CORE_IF_FILE_TRANSFER_FLOW","lastUpdated":"2020-11-04T20:27:05.000Z","contact":{"name":"EIS Front Door","emailAddress":"services.enterpriseintegration@hmrc.gov.uk"},"sourceSystem":["BMC"],"targetSystem":["ServiceNow"],"fileTransferPattern":"Corporate to corporate"}"""
      val expectedFtJsonContent2 =
        """{"publisherReference":"BVD-DPS-PCPMonthly-pull","fileTransferSpecificationVersion":"0.1","title":"BVD-DPS-PCPMonthly-pull","description":"A file transfer from Birth Verification Data (BVD) to Data Provisioning Systems (DPS)","platformType":"CORE_IF_FILE_TRANSFER_FLOW","lastUpdated":"2020-11-04T20:27:05.000Z","contact":{"name":"EIS Front Door","emailAddress":"services.enterpriseintegration@hmrc.gov.uk"},"sourceSystem":["BVD"],"targetSystem":["DPS"],"fileTransferPattern":"Corporate to corporate"}"""

      verify(mockPublisherConnector).publishFileTransfer(expectedFtJsonContent1)
      verify(mockPublisherConnector).publishFileTransfer(expectedFtJsonContent2)
    }

    "be unsucessfull if publish returns a non 2xx" in new Setup {
      when(mockPublisherConnector.publishFileTransfer((*)))
        .thenReturn(Right(Response(200, "")))
        .andThen(Right(Response(400, "Mock respose for invalid FT Json")))

      val result = service.publishDirectory(directoryPath)

      result shouldBe Left("Failed to publish 'BVD-DPS-PCPMonthly-pull.json'. Response(400): Mock respose for invalid FT Json")
    }

    "be unsucessfull if passed a file instead of a directory" in new Setup {

      val invalidDirectoryPath = directoryPath + filename2
      val result               = service.publishDirectory(invalidDirectoryPath)

      result shouldBe Left(s"`$invalidDirectoryPath` is not a directory")
    }
  }
}
