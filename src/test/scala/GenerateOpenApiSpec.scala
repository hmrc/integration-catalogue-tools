import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.io.Source
import io.swagger.v3.oas.models.OpenAPI

import scala.collection.JavaConverters._

class GenerateOpenApiSpec extends AnyWordSpec with Matchers {
  "my tests" in {
    val csvFile = Source.fromURL(getClass.getResource("/file1.csv"))
    
    val apis : Seq[OpenAPI] = GenerateOpenApi.fromCsv(csvFile.bufferedReader())

    apis should have length 1

    val api = apis.head

    // TODO: Publisher Ref?
    api.getInfo().getTitle() shouldBe "My API Title"
    api.getInfo().getDescription() shouldBe "My API Description"
    api.getInfo().getVersion() shouldBe "1.0"
    
    Option(api.getPaths().get("/my/resource/uri").getGet()).isDefined shouldBe true
  }
}
