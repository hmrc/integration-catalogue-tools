package uk.gov.hmrc.integrationcataloguetools

import io.swagger.v3.oas.models.info.{Contact, Info}
import io.swagger.v3.oas.models.media.{Content, MediaType}
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem, Paths}
import io.swagger.v3.oas.models.responses.{ApiResponses, ApiResponse}

import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.JavaConverters._
import java.util.HashMap
import java.io.FileReader

import io.swagger.v3.core.util.Yaml

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.Reader

import uk.gov.hmrc.integrationcataloguetools.models._

object GenerateOpenApi {
  def fromCsvToOasContent(reader : Reader, platform: Platform) : Seq[(PublisherReference, String)] = {
    fromCsvToOpenAPI(reader, platform).map{case (publisherReference, openApi) => {
      (publisherReference, openApiToContent(openApi, platform))
    }}
  }

  def fromCsvToOpenAPI(reader : Reader, platform: Platform) : Seq[(PublisherReference, OpenAPI)] = {

    def createBasicApi(record: CSVRecord) : BasicApi = {
      val expectedValues = 6
      // TODO : Handle without exception?
      if (record.size() < 6) throw new RuntimeException(s"Expected $expectedValues values on row ${record.getRecordNumber()}")

      def parseString(s: String) : String = {
        Option(s).getOrElse("").trim()
      }

      BasicApi(
        PublisherReference(parseString(record.get(0))),
        parseString(record.get(1)),
        parseString(record.get(2)),
        parseString(record.get(3)),
        parseString(record.get(4)),
        parseString(record.get(5)) )
      }

    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .parse(reader).getRecords().asScala.toSeq
      .map(createBasicApi)
      .map(basicApi => (basicApi.publisherReference, createOpenApi(basicApi, platform)) )
  }

  def generateOasContent(basicApi: BasicApi, platform: Platform): String = {
    val openApi : OpenAPI = createOpenApi(basicApi, platform)

    openApiToContent(openApi, platform)
  }

  def openApiToContent(openApi: OpenAPI, platform: Platform) : String = {
    Yaml.mapper().writeValueAsString(openApi);
  }

  private def createOpenApi(basicApi: BasicApi, platform: Platform): OpenAPI = {
    val openApiInfo = new Info()
    openApiInfo.setTitle(basicApi.title)
    openApiInfo.setDescription(basicApi.description)
    openApiInfo.setVersion(basicApi.version)

    val integrationCatalogueExtensions = new HashMap[String, Object]
    integrationCatalogueExtensions.put("platform", platform.value)
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

    pathItem
  }



  def createOperation(basicApi: BasicApi) : Operation = {
    
    val operation = new Operation()
      
    // getOperation.setDescription(oasGetEndpointDesc)
    
    // operation.setSummary(basicApi.title) // TODO Summary used as the title on the current FE. Probably don't need to do this
    // operation.setRequestBody(createRequest())
    
    operation.setResponses(createResponses())

    operation
  }

  private def createRequest() : RequestBody = {
    val mapper = new ObjectMapper()
    val jsonNodeVal = mapper.readTree("{\"SomeValue\": \"theValue\"}")
    val content1Example = new io.swagger.v3.oas.models.examples.Example()
    content1Example.setValue(jsonNodeVal)

    val content1MediaType = new MediaType()
    content1MediaType.addExamples("TODO Example Description", content1Example) // TODO

    val rbContent1 = new Content()
    rbContent1.put("application/json", content1MediaType) // TODO

    val requestBody1 = new RequestBody()
    // requestBody1.setContent(rbContent1)
    requestBody1
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
