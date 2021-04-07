package uk.gov.hmrc.integrationcataloguetools.connector

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.mockito.Mock
import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchersSugar
import org.apache.http.impl.client.CloseableHttpClient
import uk.gov.hmrc.integrationcataloguetools.connectors.PublisherConnector
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.StringEntity
import org.apache.http.entity.ContentType
import org.apache.http.HttpResponse
import org.apache.http.HttpEntity
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import org.apache.http.StatusLine
import org.apache.http.message.BasicStatusLine
import uk.gov.hmrc.integrationcataloguetools.connectors.Response
import uk.gov.hmrc.integrationcataloguetools.models.Platform
import org.apache.http.ProtocolVersion
import org.scalatest.BeforeAndAfterEach


class PublisherConnectorSpec extends AnyWordSpec with Matchers with MockitoSugar with ArgumentMatchersSugar with BeforeAndAfterEach {

  val mockCloseableHttpClient = mock[CloseableHttpClient]
  val url = ""

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCloseableHttpClient)
  }

  trait Setup {

    val encodedAuthHeader = "dGhpc2lzYXNzZWN1cmVhc2l0Z2V0cw=="
    val platform = Platform("CORE_IF")
    val connector = new PublisherConnector(url, mockCloseableHttpClient, platform, encodedAuthHeader)
    val fileTransferJsonContent = "File Transfer Json content"
    val entity: StringEntity = new StringEntity(fileTransferJsonContent, ContentType.create("application/json", "UTF-8"));
    val mockCloseableHttpResponse = mock[CloseableHttpResponse]
    val mockHttpEntity = mock[HttpEntity]
    val responseContent = "Happy path response"
    val dummyInputStreamFT = new ByteArrayInputStream(responseContent.getBytes(StandardCharsets.UTF_8));

    val headers = Map(
      "x-specification-type" -> "OAS_V3",
      "x-publisher-reference" -> "1234"
    )

    def primeHappyPath() {
      when(mockHttpEntity.getContent()).thenReturn(dummyInputStreamFT)
      when(mockCloseableHttpResponse.getEntity()).thenReturn(mockHttpEntity)
      when(mockCloseableHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 0), 200, ""))
    }
  }

  "publishFileTransfer" should {
    "return Right succesful response" in new Setup {

      primeHappyPath()
      when(mockCloseableHttpClient.execute(any[HttpPut])).thenReturn(mockCloseableHttpResponse)

      val response: Either[String, Response] = connector.publishFileTransfer(fileTransferJsonContent)

      response match {
        case Left(_)  => fail()
        case Right(_) => succeed
      }

    }

    "return Left after client throws and exception" in new Setup {
      when(mockCloseableHttpClient.execute(any[HttpPut])).thenThrow(new RuntimeException(""))

      val response: Either[String, Response] = connector.publishFileTransfer(fileTransferJsonContent)

      response match {
        case Left(_)  => succeed
        case Right(_) => fail
      }

    }

  }

  "publishApi" should {
    "return Right succesful response" in new Setup {
      primeHappyPath()
      when(mockCloseableHttpClient.execute(any[HttpPut])).thenReturn(mockCloseableHttpResponse)

      val response: Either[String, Response] = connector.publishApi(headers, "filename", fileTransferJsonContent.getBytes(StandardCharsets.UTF_8))

      response match {
        case Left(_)  => fail()
        case Right(_) => succeed
      }

    }

    "return Left after client throws and exception" in new Setup {
      when(mockCloseableHttpClient.execute(any[HttpPut])).thenThrow(new RuntimeException(""))

      val response: Either[String, Response] = connector.publishApi(headers, "filename", fileTransferJsonContent.getBytes(StandardCharsets.UTF_8))

      response match {
        case Left(_)  => succeed
        case Right(_) => fail
      }

    }
  }
}
