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
import scala.io.Source
import io.swagger.v3.oas.models.OpenAPI

import scala.collection.JavaConverters._
import uk.gov.hmrc.integrationcataloguetools.models.Platform
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.responses.ApiResponses

class GenerateOpenApiSpec extends AnyWordSpec with Matchers {

  val testPlatform = Platform("TEST_PLATFORM")

  trait Setup {

    def validateParameters(pathItem: Option[PathItem]) = {
      val parameters = Option(pathItem.get.getParameters()).getOrElse(new java.util.ArrayList()).asScala.toList
      parameters.size shouldBe 1
      parameters.headOption.get.getName() shouldBe "uri"
      parameters.headOption.get.getRequired shouldBe true
      parameters.headOption.get.getIn() shouldBe "path"
      parameters.headOption.get.getSchema().getType shouldBe "string"

    }

    def validateInfo(api: OpenAPI) = {
      api.getInfo().getTitle() shouldBe "My API Title"
      api.getInfo().getDescription() shouldBe "My API Description"
      api.getInfo().getVersion() shouldBe "1.0"
    }

    def validateExtensions(api: OpenAPI) = {
      val integrationCatalogueExtensions = api.getInfo().getExtensions().get("x-integration-catalogue").asInstanceOf[java.util.HashMap[String, Object]]
      integrationCatalogueExtensions.get("platform").asInstanceOf[String] shouldBe testPlatform.value
      integrationCatalogueExtensions.get("publisher-reference").asInstanceOf[String] shouldBe "My Ref 123"
    }

    def validateResponses(responses: ApiResponses) = {
      val responseOK = Option(responses.get("200"))
      responseOK.isDefined shouldBe true
      responseOK.get.getDescription() shouldBe "OK"
      val responseBadRequest = Option(responses.get("400"))
      responseBadRequest.isDefined shouldBe true
      responseBadRequest.get.getDescription() shouldBe "Bad request"

    }

    def commonValidation(filepath: String, piToOp: PathItem => Operation, validateParams: Boolean = true, endpoint: String = "/my/resource/uri/{uri}") = {
      val csvFile = Source.fromResource(filepath)

      val apis: Seq[(_, OpenAPI)] = GenerateOpenApi.fromCsvToOpenAPI(csvFile.bufferedReader())

      apis should have length 1

      val (_, api) = apis.head

      validateInfo(api)

      validateExtensions(api)

      val pathItem = Option(api.getPaths().get(endpoint))

      if (validateParams) { validateParameters(pathItem) }

      val maybeOperation = pathItem.map(piToOp.apply(_))
      maybeOperation.isDefined shouldBe true

      Option(maybeOperation.get.getResponses).map(validateResponses)
        .getOrElse(fail("response should not be empty"))
    }
  }

  "Parse CSV with GET endpoint into OpenAPI list" in new Setup {
    commonValidation("generateopenapispec/Parse-CSV-into-OpenAPI-list.csv", pathItem => pathItem.getGet())

  }

  "Parse CSV with POST endpoint into OpenAPI list" in new Setup {
    commonValidation("generateopenapispec/Parse-CSV-with-POST-into-OpenAPI.csv", pathItem => pathItem.getPost())

  }

  "Parse CSV with PUT endpoint into OpenAPI list" in new Setup {
    commonValidation("generateopenapispec/Parse-CSV-with-PUT-into-OpenAPI.csv", pathItem => pathItem.getPut())

  }

  "Parse CSV with PATCH endpoint into OpenAPI list" in new Setup {
    commonValidation("generateopenapispec/Parse-CSV-with-PATCH-into-OpenAPI.csv", pathItem => pathItem.getPatch())

  }

  "Parse CSV with DELETE endpoint into OpenAPI list" in new Setup {
    commonValidation("generateopenapispec/Parse-CSV-with-DELETE-into-OpenAPI.csv", pathItem => pathItem.getDelete())

  }

  "Parse CSV with OPTIONS endpoint into OpenAPI list" in new Setup {
    commonValidation("generateopenapispec/Parse-CSV-with-OPTIONS-into-OpenAPI.csv", pathItem => pathItem.getOptions())

  }

  "Parse CSV with HEAD endpoint into OpenAPI list" in new Setup {
    commonValidation("generateopenapispec/Parse-CSV-with-HEAD-into-OpenAPI.csv", pathItem => pathItem.getHead())

  }

  "Parse CSV with UNKNOWN endpoint into OpenAPI list" in new Setup {
    commonValidation("generateopenapispec/Parse-CSV-with-UNKNOWN-method-into-OpenAPI.csv", pathItem => pathItem.getGet())

  }

  "throw RuntimeException when number of invalid CSV" in {
    val csvFile = Source.fromResource("generateopenapispec/Parse-CSV-with-5-columns-into-OpenAPI.csv")

    val exception = intercept[RuntimeException] { GenerateOpenApi.fromCsvToOpenAPI(csvFile.bufferedReader()) }

    exception.getMessage shouldBe "Expected 9 values on row 1"
  }

  "Parse CSV with no parameters into OpenAPI list" in new Setup {
    commonValidation("generateopenapispec/Parse-CSV-without-pathParameters-into-OpenAPI.csv", pathItem => pathItem.getGet(), false, "/my/resource/uri")
  }

  "Parse CSV into OpenAPI Specification content" in {
    val csvFile = Source.fromResource("generateopenapispec/Parse-to-OAS-Content.csv")
    val expectedContent = Source.fromResource("generateopenapispec/Expected-Parse-to-OAS-Content.yaml").mkString

    val contents = GenerateOpenApi.fromCsvToOasContent(csvFile.bufferedReader())

    contents should have length 1

    val content = contents.head._2

    content shouldBe expectedContent
  }

  "Parse CSV with Path Parameters into OpenAPI Specification content" in {
    val csvFile = Source.fromResource("generateopenapispec/Parse-CSV-into-OpenAPI-list.csv")
    val expectedContent = Source.fromResource("generateopenapispec/Expected-Parse-CSV-into-OpenAPI-list.yaml").mkString

    val contents = GenerateOpenApi.fromCsvToOasContent(csvFile.bufferedReader())

    contents should have length 1

    val content = contents.head._2

    content shouldBe expectedContent
  }

  "Parse CSV with Query Parameters into OpenAPI Specification with no Query Parameters" in {
    val csvFile = Source.fromResource("generateopenapispec/Parse-CSV-with-queryParameters-into-OpenAPI.csv")
    val expectedContent = Source.fromResource("generateopenapispec/Expected-Parse-CSV-with-query-params-into-OpenAPI.yaml").mkString

    val contents = GenerateOpenApi.fromCsvToOasContent(csvFile.bufferedReader())

    contents should have length 1

    val content = contents.head._2

    content shouldBe expectedContent
  }
}
