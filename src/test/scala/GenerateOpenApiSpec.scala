import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.io.Source
import io.swagger.v3.oas.models.OpenAPI

import scala.collection.JavaConverters._

class GenerateOpenApiSpec extends AnyWordSpec with Matchers {
  "Parse CSV into OpenAPI list" in {
    val csvFile = Source.fromResource("generateopenapispec/Parse-CSV-into-OpenAPI-list.csv")
    
    val apis : Seq[(_, OpenAPI)] = GenerateOpenApi.fromCsvToOpenAPI(csvFile.bufferedReader())

    apis should have length 1

    val (publisherReference, api) = apis.head

    // TODO: Publisher Ref?
    api.getInfo().getTitle() shouldBe "My API Title"
    api.getInfo().getDescription() shouldBe "My API Description"
    api.getInfo().getVersion() shouldBe "1.0"
    
    Option(api.getPaths().get("/my/resource/uri").getGet()).isDefined shouldBe true

    // TODO : Test other fields? Summary, responses...
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
