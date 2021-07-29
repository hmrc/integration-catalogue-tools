/*
 * Copyright 2021 HM Revenue & Customs
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

import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.{ApiResponse, ApiResponses}
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem, Paths}
import org.apache.commons.csv.CSVRecord
import uk.gov.hmrc.integrationcataloguetools.models._

import java.io.Reader
import java.util
import scala.collection.JavaConverters._

object GenerateOpenApi {

  def fromCsvToOasContent(reader: Reader): Seq[(PublisherReference, String)] = {
    fromCsvToOpenAPI(reader).map {
      case (publisherReference, openApi) =>
        (publisherReference, openApiToContent(openApi))
    }
  }

  def fromCsvToOpenAPI(reader: Reader): Seq[(PublisherReference, OpenAPI)] = {

    def createBasicApi(record: CSVRecord): BasicApi = {
      val expectedValues = 9
      // TODO : Handle without exception?
      if (record.size() < expectedValues) throw new RuntimeException(s"Expected $expectedValues values on row ${record.getRecordNumber}")

      def parseString(s: String): String = {
        Option(s).getOrElse("").trim()
      }

      def parseStatus(s: String): String = {
        Option(s).getOrElse("LIVE").trim()
      }


      def truncateAfter(x: String, p: String) = {
        val s = parseString(x)
        if (s.indexOf(p) > 0) s.substring(0, s.indexOf(p)) else s
      }

      def parsePathParameters(endpoint: String): List[String] = {
        truncateAfter(endpoint, "?")
          .split("/")
          .filter(path => path.contains("{") && path.contains("}"))
          .map(pathParam => pathParam.replace("{", "").replace("}", ""))
          .toList
      }

      def removeQueryParametersFromUrl(url: String): String = {
        truncateAfter(url, "?").replace("?", "")
      }

      BasicApi(
        PublisherReference(value = parseString(record.get(0))),
        platform = Platform(parseString(record.get(1))),
        title = parseString(record.get(2)),
        description = parseString(record.get(3)),
        version = parseString(record.get(4)),
        method = parseString(record.get(5)),
        endpoint = removeQueryParametersFromUrl(parseString(record.get(6))),
        parameters = parsePathParameters(record.get(6)),
        status = Status(parseStatus(record.get(7))),
        reviewedDate = parseString(record.get(8))
      )
    }

    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .parse(reader).getRecords.asScala
      .map(createBasicApi)
      .map(basicApi => (basicApi.publisherReference, createOpenApi(basicApi)))
  }

  def generateOasContent(basicApi: BasicApi): String = {
    val openApi: OpenAPI = createOpenApi(basicApi)

    openApiToContent(openApi)
  }

  def openApiToContent(openApi: OpenAPI): String = {
    Yaml.mapper().writeValueAsString(openApi)
  }

  private def createOpenApi(basicApi: BasicApi): OpenAPI = {

    val openApiInfo = new Info()
    openApiInfo.setTitle(basicApi.title)
    openApiInfo.setDescription(basicApi.description)
    openApiInfo.setVersion(basicApi.version)

    val integrationCatalogueExtensions = new util.HashMap[String, Object]
    integrationCatalogueExtensions.put("platform", basicApi.platform.value)
    integrationCatalogueExtensions.put("publisher-reference", basicApi.publisherReference.value)
    integrationCatalogueExtensions.put("status", basicApi.status.value)
    integrationCatalogueExtensions.put("reviewed-date", basicApi.reviewedDate)

    val oasExtensions = new util.HashMap[String, Object]()
    oasExtensions.put("x-integration-catalogue", integrationCatalogueExtensions)

    openApiInfo.setExtensions(oasExtensions)

    val pathItem = getPathItem(basicApi)

    val paths = new Paths()
    paths.addPathItem(getEndpointUrl(basicApi), pathItem)

    val openApi = new OpenAPI()
    openApi.setInfo(openApiInfo)
    openApi.setPaths(paths)

    openApi
  }

  def getEndpointUrl(basicApi: BasicApi): String = {
    if (basicApi.endpoint.startsWith("/")) {
      basicApi.endpoint
    } else {
      val error = s"Invalid path '${basicApi.endpoint}' for publisherReference '${basicApi.publisherReference}'"
      println(error)
      "/unknown"
    }
  }

  def getPathItem(basicApi: BasicApi): PathItem = {
    val parameters = createParameters(basicApi)

    val pathItem = new PathItem()
    basicApi.method.toUpperCase.trim match {
      case "GET"     => pathItem.setGet(createOperation())
      case "POST"    => pathItem.setPost(createOperation())
      case "PUT"     => pathItem.setPut(createOperation())
      case "PATCH"   => pathItem.setPatch(createOperation())
      case "DELETE"  => pathItem.setDelete(createOperation())
      case "OPTIONS" => pathItem.setOptions(createOperation())
      case "HEAD"    => pathItem.setHead(createOperation())
      case unknown   => // TODO Handle / filter these?
        val error = s"Unsupported method: '$unknown' for publisherReference '${basicApi.publisherReference}'"
        println(error)
        pathItem.setGet(createOperation())
    }
    if (parameters.nonEmpty) pathItem.setParameters(parameters.asJava)
    pathItem
  }

  def createOperation(): Operation = {
    val operation = new Operation()
    operation.setResponses(createResponses())
    operation
  }

  def createParameters(basicApi: BasicApi): List[Parameter] = {
    basicApi.parameters.map(param => {
      val paramObj = new Parameter()
      paramObj.setName(param)
      paramObj.setIn("path")
      paramObj.setRequired(true)
      val schema = new Schema()
      schema.setType("string")
      paramObj.setSchema(schema)
      paramObj
    })
  }

  private def createResponses(): ApiResponses = {
    val ok = new ApiResponse()
    ok.setDescription("OK")

    val badRequest = new ApiResponse()
    badRequest.setDescription("Bad request")

    val responseBodies = new ApiResponses()
    responseBodies.addApiResponse("200", ok)
    responseBodies.addApiResponse("400", badRequest)

    responseBodies
  }
}
