package uk.gov.hmrc.integrationcataloguetools

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.io.Source
import io.swagger.v3.oas.models.OpenAPI

import uk.gov.hmrc.integrationcataloguetools.connectors._

import scala.collection.JavaConverters._
import org.mockito.Mock
import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchersSugar
import java.nio.charset.StandardCharsets

class PuslisherServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ArgumentMatchersSugar {

  val testResourcesPath = "src/test/resources/publishservicespec/"
  val desPlatform = Platform("DES") 
  val specificaitonType = "OAS_V3"
  
  trait Setup {
    val mockPublisherConnector = mock[PublisherConnector]
    val service = new PublisherService(mockPublisherConnector)
  }

  "Publish single file" should {
    val filename1 = "example-oas-1.yaml"
    val filepath1 = testResourcesPath + filename1

    "be sucessfull if publish returns a 200" in new Setup {
      when(mockPublisherConnector.publish( (*) , (*) ,(*) )).thenReturn(Right(Response(200, "")))

      val result = service.publishFile(desPlatform, filepath1)

      result shouldBe Right()

      val expectedHeaders = Map(
          "x-platform-type" -> desPlatform.value,
          "x-specification-type" -> specificaitonType,
          "x-publisher-reference" -> filename1
        )

      val expectedOasContent = "OAS File Content\n".getBytes(StandardCharsets.US_ASCII);
      verify(mockPublisherConnector).publish(expectedHeaders, filename1, expectedOasContent)
    }

    "be sucessfull if publish returns a 201" in new Setup {
      when(mockPublisherConnector.publish( (*) , (*) ,(*) )).thenReturn(Right(Response(201, "")))

      val result = service.publishFile(desPlatform, filepath1)

      result shouldBe Right()
    }
  
    "be unsucessfull if publish returns a non 2xx" in new Setup {
      val publishErrorBody = "Failed to publish - 400"
      when(mockPublisherConnector.publish( (*) , (*) ,(*) ))
        .thenReturn(Right(Response(400, publishErrorBody)))

      val result = service.publishFile(desPlatform, filepath1)

      val expectedErrorMessage = s"Failed to publish '$filename1'. Response(400): $publishErrorBody"

      result shouldBe Left(expectedErrorMessage)
    }
  }

  "Publish directory of files" should {
    val filename2 = "example-oas-2.yaml"
    val filename3 = "example-oas-3.json"

    val directoryPath = testResourcesPath + "directory-of-files-1"

    "be sucessfull if publish returns a 2xx" in new Setup {
      when(mockPublisherConnector.publish( (*) , (*) ,(*) ))
        .thenReturn(Right(Response(200, "")))

      val result = service.publishDirectory(desPlatform, directoryPath)

      result shouldBe Right()

      val expectedHeaders1 = Map(
          "x-platform-type" -> desPlatform.value,
          "x-specification-type" -> specificaitonType,
          "x-publisher-reference" -> filename2
      )

      val expectedHeaders2 = Map(
          "x-platform-type" -> desPlatform.value,
          "x-specification-type" -> specificaitonType,
          "x-publisher-reference" -> filename3
      )

      val expectedOasContent1 = "OAS File Content 2\n".getBytes(StandardCharsets.US_ASCII);
      val expectedOasContent2 = "OAS File Content 3\n".getBytes(StandardCharsets.US_ASCII);

      verify(mockPublisherConnector).publish(expectedHeaders1, filename2, expectedOasContent1)
      verify(mockPublisherConnector).publish(expectedHeaders2, filename3, expectedOasContent2)
    }

    "be unsucessfull if publish returns a non 2xx" in new Setup {
      when(mockPublisherConnector.publish( (*) , (*) ,(*) ))
        .thenReturn(Right(Response(200, "")))
        .andThen(Right(Response(400, "Mock respose for invalid OAS")))

      val result = service.publishDirectory(desPlatform, directoryPath)

      result shouldBe Left("Failed to publish 'example-oas-3.json'. Response(400): Mock respose for invalid OAS")
    }

    "be unsucessfull if passed a file instead of a directory" in new Setup {
      
      val invalidDirectoryPath = directoryPath + filename2
      val result = service.publishDirectory(desPlatform, invalidDirectoryPath)

      result shouldBe Left(s"`$invalidDirectoryPath` is not a directory")
    }
  }
}
