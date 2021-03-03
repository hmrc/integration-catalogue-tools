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

  "Publish file" should {
    val filename1 = "example-oas-1.yaml"
    val filepath1 = "src/test/resources/publishservicespec/" + filename1

    "be sucessfull if publish returns a 200" in {
      val mockPublisherConnector = mock[PublisherConnector]
      val service = new PublisherService(mockPublisherConnector)

      when(mockPublisherConnector.publish( (*) , (*) ,(*) )).thenReturn(Right(Response(200, "")))

      val result = service.publishFile(Platform("DES"), filepath1)

      result shouldBe Right()

      val expectedHeaders = Map(
          "x-platform-type" -> "DES",
          "x-specification-type" -> "OAS_V3",
          "x-publisher-reference" -> "example-oas-1.yaml"
        )

      val expectedOasContent = "OAS File Content\n".getBytes(StandardCharsets.US_ASCII);
      verify(mockPublisherConnector).publish(expectedHeaders, filename1, expectedOasContent)
    }

    "be sucessfull if publish returns a 201" in {
      val mockPublisherConnector = mock[PublisherConnector]
      val service = new PublisherService(mockPublisherConnector)

      when(mockPublisherConnector.publish( (*) , (*) ,(*) )).thenReturn(Right(Response(201, "")))

      val result = service.publishFile(Platform("DES"), filepath1)

      result shouldBe Right()
    }
  
    "be unsucessfull if publish returns a non 2xx" in {
      val mockPublisherConnector = mock[PublisherConnector]
      val service = new PublisherService(mockPublisherConnector)

      val publishErrorBody = "Failed to publish - 400"
      when(mockPublisherConnector.publish( (*) , (*) ,(*) ))
        .thenReturn(Right(Response(400, publishErrorBody)))

      val result = service.publishFile(Platform("DES"), filepath1)

      val expectedErrorMessage = s"Failed to publish 'example-oas-1.yaml'. Response(400): $publishErrorBody"

      result shouldBe Left(expectedErrorMessage)
    }
  }

  "Publish directory of files" in {
    // TODO
  }
}
