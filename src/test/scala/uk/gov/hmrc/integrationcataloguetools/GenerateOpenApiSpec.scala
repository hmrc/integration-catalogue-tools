package uk.gov.hmrc.integrationcataloguetools

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.io.Source
import io.swagger.v3.oas.models.OpenAPI

import scala.collection.JavaConverters._
import uk.gov.hmrc.integrationcataloguetools.models.Platform

class GenerateOpenApiSpec extends AnyWordSpec with Matchers {
  val testPlatform = Platform("TEST_PLATFORM")
  "Parse CSV into OpenAPI list" in {
    val csvFile = Source.fromResource("generateopenapispec/Parse-CSV-into-OpenAPI-list.csv")
    
    val apis : Seq[(_, OpenAPI)] = GenerateOpenApi.fromCsvToOpenAPI(csvFile.bufferedReader())

    apis should have length 1

    val (publisherReference, api) = apis.head

    api.getInfo().getTitle() shouldBe "My API Title"
    api.getInfo().getDescription() shouldBe "My API Description"
    api.getInfo().getVersion() shouldBe "1.0"

    val integrationCatalogueExtensions = api.getInfo().getExtensions().get("x-integration-catalogue").asInstanceOf[java.util.HashMap[String, Object]]
    integrationCatalogueExtensions.get("platform").asInstanceOf[String] shouldBe testPlatform.value
    integrationCatalogueExtensions.get("publisher-reference").asInstanceOf[String] shouldBe "My Ref 123"
    
    Option(api.getPaths().get("/my/resource/uri").getGet()).isDefined shouldBe true
  }

  "Parse CSV into OpenAPI Specification content" in {
    val csvFile = Source.fromResource("generateopenapispec/Parse-to-OAS-Content.csv")
    val expectedContent = Source.fromResource("generateopenapispec/Expected-Parse-to-OAS-Content.yaml").mkString

    val contents = GenerateOpenApi.fromCsvToOasContent(csvFile.bufferedReader())

    contents should have length 1

    val content = contents.head._2

    content shouldBe expectedContent
  }
}
