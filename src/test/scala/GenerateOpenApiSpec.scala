import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.io.Source
import io.swagger.v3.oas.models.OpenAPI

import scala.collection.JavaConverters._

class GenerateOpenApiSpec extends AnyWordSpec with Matchers {
  "Parse CSV into OpenAPI list" in {
    val csvFile = Source.fromURL(getClass.getResource("/Parse-CSV-into-OpenAPI-list.csv"))
    
    val apis : Seq[(String, OpenAPI)] = GenerateOpenApi.fromCsv(csvFile.bufferedReader())

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

  }
}
