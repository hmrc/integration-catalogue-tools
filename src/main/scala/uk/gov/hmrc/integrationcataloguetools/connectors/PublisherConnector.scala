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

package uk.gov.hmrc.integrationcataloguetools.connectors

import org.apache.http.client.methods.{CloseableHttpResponse, HttpPut}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.CloseableHttpClient
import uk.gov.hmrc.integrationcataloguetools.models.Platform

import scala.util.{Failure, Success, Try}

case class Response(statusCode: Int, content: String)

class PublisherConnector(url: String, client: CloseableHttpClient, platform: Platform, authorizationKey: String) {

  def publishApi(headers: Map[String, String], filename: String, oasContent: Array[Byte]): Either[String, Response] = {
    import org.apache.http.entity.ContentType
    import org.apache.http.entity.mime.MultipartEntityBuilder

    val put = new HttpPut(url)
    headers.foreach(header => put.addHeader(header._1, header._2))

    val entity = MultipartEntityBuilder.create()
    entity.addBinaryBody("selectedFile", oasContent, ContentType.DEFAULT_TEXT, filename)
    put.setEntity(entity.build())

    callEndpoint(put)

  }

  def publishFileTransfer(content: String): Either[String, Response] = {

    val put = new HttpPut(url)

    val entity: StringEntity = new StringEntity(content, ContentType.create("application/json", "UTF-8"))
    put.setEntity(entity)

    callEndpoint(put)
  }

  private def callEndpoint(put: HttpPut): Either[String, Response] = {
    
    put.addHeader("Authorization", authorizationKey)
    put.addHeader("x-platform-type", platform.value)

    Try(client.execute(put))
      .map((response: CloseableHttpResponse) => {
        val content = scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
        Response(response.getStatusLine.getStatusCode, content)
      }) match {
      case Success(response)  => Right(response)
      case Failure(exception) =>
        println("Error calling publish service:")
        exception.printStackTrace()
        Left(exception.getMessage)
    }
  }

}
