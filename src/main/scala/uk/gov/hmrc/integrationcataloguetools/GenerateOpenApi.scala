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
import java.util.HashMap
import scala.collection.JavaConverters._

object GenerateOpenApi {
  def fromCsvToOasContent(reader : Reader) : Seq[(PublisherReference, String)] = {
    fromCsvToOpenAPI(reader).map{case (publisherReference, openApi) => {
      (publisherReference, openApiToContent(openApi))
    }}
  }

  def fromCsvToOpenAPI(reader : Reader) : Seq[(PublisherReference, OpenAPI)] = {

    def createBasicApi(record: CSVRecord) : BasicApi = {
      val expectedValues = 6
      // TODO : Handle without exception?
      if (record.size() < 6) throw new RuntimeException(s"Expected $expectedValues values on row ${record.getRecordNumber()}")

      def parseString(s: String) : String = {
        Option(s).getOrElse("").trim()
      }

      def truncateAfter(x: String, p: String) = {
       val s =  parseString(x)
        if(s.indexOf(p) > 0) s.substring(0, s.indexOf(p)) else s
      }
      
      def parsePathParameters(endpoint: String) : List[String] = {
        truncateAfter(endpoint, "?")
        .split("/")
        .filter(path => path.contains("{") && path.contains("}"))
        .map(pathParam => pathParam.replace("{", "").replace("}", ""))
        .toList
      }

      BasicApi(
        PublisherReference(parseString(record.get(0))),
        Platform(parseString(record.get(1))),
        parseString(record.get(2)),
        parseString(record.get(3)),
        parseString(record.get(4)),
        parseString(record.get(5)),
        parseString(record.get(6)),
        parsePathParameters(record.get(6))
      )}

    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .parse(reader).getRecords().asScala.toSeq
      .map(createBasicApi)
      .map(basicApi => (basicApi.publisherReference, createOpenApi(basicApi)) )
  }

  def generateOasContent(basicApi: BasicApi): String = {
    val openApi : OpenAPI = createOpenApi(basicApi)

    openApiToContent(openApi)
  }

  def openApiToContent(openApi: OpenAPI) : String = {
    Yaml.mapper().writeValueAsString(openApi);
  }

  private def createOpenApi(basicApi: BasicApi): OpenAPI = {
    println(basicApi)
    val openApiInfo = new Info()
    openApiInfo.setTitle(basicApi.title)
    openApiInfo.setDescription(basicApi.description)
    openApiInfo.setVersion(basicApi.version)

    val integrationCatalogueExtensions = new HashMap[String, Object]
    integrationCatalogueExtensions.put("platform", basicApi.platform.value)
    integrationCatalogueExtensions.put("publisher-reference", basicApi.publisherReference.value)

    val oasExtensions = new HashMap[String, Object]()
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

  def getEndpointUrl(basicApi: BasicApi) : String = {
    val pathsObj = new Paths()
    if (basicApi.endpoint.startsWith("/")) {
      basicApi.endpoint
    } else {
      val error = s"Invalid path '${basicApi.endpoint}' for publisherReference '${basicApi.publisherReference}'"
      println(error)
      "/unknown"
    }
  }

  def getPathItem(basicApi: BasicApi) : PathItem = {
    val operation = createOperation(basicApi)
    val parameters = createParameters(basicApi)

    val pathItem = new PathItem()
    basicApi.method.toUpperCase.trim match {
      case "GET" => pathItem.setGet(operation)
      case "POST" => pathItem.setPost(operation)
      case "PUT" => pathItem.setPut(operation)
      case "PATCH" => pathItem.setPatch(operation)
      case "DELETE" => pathItem.setDelete(operation)
      case "OPTIONS" => pathItem.setOptions(operation)
      case "HEAD" => pathItem.setHead(operation)
      case unknown => { // TODO Handle / filter these?
        val error = s"Unsupported method: '$unknown' for publisherReference '${basicApi.publisherReference}'"
        // throw new RuntimeException(s"error")
        println(error)
        
        pathItem.setGet(operation)
      }
    }
    if(parameters.nonEmpty) pathItem.setParameters(parameters.asJava)
    pathItem
  }

  def createOperation(basicApi: BasicApi) : Operation = {
    val operation = new Operation()
    operation.setResponses(createResponses())
    operation
  }
  
  def createParameters(basicApi: BasicApi) : List[Parameter] = {
      basicApi.parameters.map(param => {
       val paramObj =  new Parameter()
       println(s"**** $param ******")
       paramObj.setName(param)
       paramObj.setIn("path")
       paramObj.setRequired(true)
       val schema = new Schema()
       schema.setType("string")
       paramObj.setSchema(schema)
       paramObj
      })
  }

  private def createResponses() : ApiResponses = {
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
