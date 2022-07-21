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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import uk.gov.hmrc.integrationcataloguetools.connectors._

import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchersSugar
import java.nio.charset.StandardCharsets
import uk.gov.hmrc.integrationcataloguetools.models.Platform
import uk.gov.hmrc.integrationcataloguetools.service.ApiPublisherService

class ApiPublisherServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ArgumentMatchersSugar {

  val testResourcesPath = "src/test/resources/publishservicespec/"
  val desPlatform = Platform("DES") 
  val specificationType = "OAS_V3"
  
  trait Setup {
    val mockPublisherConnector = mock[PublisherConnector]
    val service = new ApiPublisherService(mockPublisherConnector)
  }

  "Publish single file" should {
    val publisherReference1 = "example-oas-1"
    val filename1 = publisherReference1 + ".yaml"
    val filepath1 = testResourcesPath + filename1

    "be sucessfull if publish returns a 200 when useFilenameAsPublisherReference is true" in new Setup {
      when(mockPublisherConnector.publishApi( (*) , (*) ,(*) )).thenReturn(Right(Response(200, "")))

      val result = service.publishFile(filepath1, true)

      result shouldBe Right(())

      val expectedHeaders = Map(
          "x-specification-type" -> specificationType,
          "x-publisher-reference" -> publisherReference1
        )

      val expectedOasContent = "OAS File Content\n".getBytes(StandardCharsets.US_ASCII);
      verify(mockPublisherConnector).publishApi(expectedHeaders, filename1, expectedOasContent)
    }

    "be sucessfull if publish returns a 200 when useFilenameAsPublisherReference is false" in new Setup {
      when(mockPublisherConnector.publishApi( (*) , (*) ,(*) )).thenReturn(Right(Response(200, "")))

      val result = service.publishFile(filepath1, false)

      result shouldBe Right(())

      val expectedHeaders = Map(
          "x-specification-type" -> specificationType)

      val expectedOasContent = "OAS File Content\n".getBytes(StandardCharsets.US_ASCII);
      verify(mockPublisherConnector).publishApi(expectedHeaders, filename1, expectedOasContent)
    }

    "be sucessfull if publish returns a 201 when useFilenameAsPublisherReference is true" in new Setup {
      when(mockPublisherConnector.publishApi( (*) , (*) ,(*) )).thenReturn(Right(Response(201, "")))

      val result = service.publishFile(filepath1, true)

      result shouldBe Right(())
    }
  
    "be unsucessfull if publish returns a non 2xx when useFilenameAsPublisherReference is true" in new Setup {
      val publishErrorBody = "Failed to publish - 400"
      when(mockPublisherConnector.publishApi( (*) , (*) ,(*) ))
        .thenReturn(Right(Response(400, publishErrorBody)))

      val result = service.publishFile(filepath1, true)

      val expectedErrorMessage = s"Failed to publish '$filename1'. Response(400): $publishErrorBody"

      result shouldBe Left(expectedErrorMessage)
    }
  }

  "Publish directory of files" should {
    val publisherReference2 = "example-oas-2"
    val publisherReference3 = "example-oas-3"

    val filename2 = publisherReference2 + ".yaml"
    val filename3 = publisherReference3 + ".json"

    val directoryPath = testResourcesPath + "directory-of-files-1"

    "be sucessfull if publish returns a 2xx when useFilenameAsPublisherReference is true" in new Setup {
      when(mockPublisherConnector.publishApi( (*) , (*) ,(*) ))
        .thenReturn(Right(Response(200, "")))

      val result = service.publishDirectory(directoryPath, true)

      result shouldBe Right(())

      val expectedHeaders1 = Map(
          "x-specification-type" -> specificationType,
          "x-publisher-reference" -> publisherReference2
      )

      val expectedHeaders2 = Map(
          "x-specification-type" -> specificationType,
          "x-publisher-reference" -> publisherReference3
      )

      val expectedOasContent1 = "OAS File Content 2\n".getBytes(StandardCharsets.US_ASCII);
      val expectedOasContent2 = "OAS File Content 3\n".getBytes(StandardCharsets.US_ASCII);

      verify(mockPublisherConnector).publishApi(expectedHeaders1, filename2, expectedOasContent1)
      verify(mockPublisherConnector).publishApi(expectedHeaders2, filename3, expectedOasContent2)
    }

    "be sucessfull if publish returns a 2xx when useFilenameAsPublisherReference is false" in new Setup {
      when(mockPublisherConnector.publishApi( (*) , (*) ,(*) ))
        .thenReturn(Right(Response(200, "")))

      val result = service.publishDirectory(directoryPath, false)

      result shouldBe Right(())

      val expectedHeaders1 = Map(
          "x-specification-type" -> specificationType)

      val expectedHeaders2 = Map(
          "x-specification-type" -> specificationType)

      val expectedOasContent1 = "OAS File Content 2\n".getBytes(StandardCharsets.US_ASCII);
      val expectedOasContent2 = "OAS File Content 3\n".getBytes(StandardCharsets.US_ASCII);

      verify(mockPublisherConnector).publishApi(expectedHeaders1, filename2, expectedOasContent1)
      verify(mockPublisherConnector).publishApi(expectedHeaders2, filename3, expectedOasContent2)
    }

    "be unsucessfull if publish returns a non 2xx when useFilenameAsPublisherReference is true" in new Setup {
      when(mockPublisherConnector.publishApi( (*) , (*) ,(*) ))
        .thenReturn(Right(Response(200, "")))
        .andThen(Right(Response(400, "Mock respose for invalid OAS")))

      val result = service.publishDirectory(directoryPath, true)

      result shouldBe Left("Failed to publish 'example-oas-3.json'. Response(400): Mock respose for invalid OAS")
    }

    "be unsucessfull if passed a file instead of a directory" in new Setup {
      
      val invalidDirectoryPath = directoryPath + filename2
      val result = service.publishDirectory(invalidDirectoryPath, true)

      result shouldBe Left(s"`$invalidDirectoryPath` is not a directory")
    }
  }
}
