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
    "be sucessfull if publish returns a 200" in {
      val mockPublisherConnector = mock[PublisherConnector]
      val service = new PublisherService(mockPublisherConnector)

      when(mockPublisherConnector.publish( (*) , (*) ,(*) )).thenReturn(Right(Response(200, "")))

      val filename = "example-oas-1.yaml"
      val filepath = "src/test/resources/publishservicespec/" + filename
      val result = service.publishFile(Platform("DES"), filepath)

      result shouldBe Right()

      val expectedHeaders = Map(
          "x-platform-type" -> "DES",
          "x-specification-type" -> "OAS_V3",
          "x-publisher-reference" -> "example-oas-1.yaml"
        )

      val expectedOasContent = "OAS File Content\n".getBytes(StandardCharsets.US_ASCII);
      verify(mockPublisherConnector).publish(expectedHeaders, filename, expectedOasContent)
    }
  
    "be unsucessfull if publish returns a non 2xx" in {
      val mockPublisherConnector = mock[PublisherConnector]
      val service = new PublisherService(mockPublisherConnector)

      val publishErrorBody = "Failed to publish - 400"
      when(mockPublisherConnector.publish( (*) , (*) ,(*) ))
        .thenReturn(Right(Response(400, publishErrorBody)))

      val filename = "example-oas-1.yaml"
      val filepath = "src/test/resources/publishservicespec/" + filename
      val result = service.publishFile(Platform("DES"), filepath)

      val expectedErrorMessage = s"Failed to publish 'example-oas-1.yaml'. Response(400): $publishErrorBody"

      result shouldBe Left(expectedErrorMessage)
    }
  }

  "Publish directory of files" in {
    // TODO
  }
}
