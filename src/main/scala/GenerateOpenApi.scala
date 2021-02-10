import io.swagger.v3.oas.models.info.{Contact, Info}
import io.swagger.v3.oas.models.media.{Content, MediaType}
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem, Paths}
import io.swagger.v3.oas.models.responses.{ApiResponses, ApiResponse}

import com.fasterxml.jackson.databind.ObjectMapper

import java.util.HashMap
import io.swagger.v3.core.util.Yaml

case class BasicApi(publisherReference: String,title: String,description: String,version: String,method: String,endpoint: String)

object GenerateOpenApi {

  def generateOasContent(basicApi: BasicApi): String = {
    val openApi : OpenAPI = generateOas(basicApi)

    openApiToText(openApi)
  }

  def openApiToText(openApi: OpenAPI) : String = {
    Yaml.mapper().writeValueAsString(openApi);
  }

  def generateOas(basicApi: BasicApi): OpenAPI = {
    val openApiInfo = new Info()
    openApiInfo.setTitle(basicApi.title)
    openApiInfo.setDescription(basicApi.description)
    openApiInfo.setVersion(basicApi.version)
    // val contact = new Contact()
    // contact.setEmail(oasContactEMail)
    // contact.setName(oasContactName)
    // openAPIInfo.setContact(contact)

    // Extensions
    // val extensions = new HashMap[String, Object]()
    // extensions.put("x-integration-catalogue-reference", "1686")
    // extensions.put("x-integration-catalogue-last-updated", "08-12-2020")
    // extensions.put("x-integration-catalogue-backends", "EDH,RTI")
    // openAPIInfo.setExtensions(extensions)
    
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
    val apiResponse = new ApiResponse()
    apiResponse.setDescription("response description") // TODO
    val apiResponseContent1 = new Content()
    val apiResponseMediaType = new MediaType()
    val apiResponseExample = new io.swagger.v3.oas.models.examples.Example()
    apiResponseExample.setValue("{\"SomeRequestValue\" : \"theValue\"}")
    apiResponseExample.setSummary("response summary")
    // apiResponseMediaType.addExamples("some example response description", apiResponseExample)
    apiResponseContent1.put("application/json", apiResponseMediaType)
    apiResponse.setContent(apiResponseContent1)

    val responseBodies = new ApiResponses()
    responseBodies.addApiResponse("200", apiResponse)

    val mapper = new ObjectMapper()
    val jsonNodeVal = mapper.readTree("{\"SomeValue\": \"theValue\"}")
    val content1Example = new io.swagger.v3.oas.models.examples.Example()
    content1Example.setValue(jsonNodeVal)

    val operation = new Operation()
    val rbContent1 = new Content
    val content1MediaType = new MediaType()
    content1MediaType.addExamples("TODO Example Description", content1Example) // TODO

    rbContent1.put("application/json", content1MediaType) // TODO

    // getOperation.setDescription(oasGetEndpointDesc)
    val requestBody1 = new RequestBody()
    requestBody1.setContent(rbContent1)

    operation.setSummary(basicApi.title) // TODO Summary used as the title on the current FE. Probably don't need to do this
    operation.setRequestBody(requestBody1)
    operation.setResponses(responseBodies)

    operation
  }
}
