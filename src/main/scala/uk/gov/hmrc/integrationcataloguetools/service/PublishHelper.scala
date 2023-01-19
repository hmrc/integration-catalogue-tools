/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.integrationcataloguetools.service

import uk.gov.hmrc.integrationcataloguetools.connectors.Response

trait PublishHelper {
  val OK      = 200
  val CREATED = 201

  def getApiPublishPhrase(statusCode: Int): String = statusCode match {
    case OK      => "Updated"
    case CREATED => "Created"
    case _       => "???"
  }

  def handlePublishResponse(responseEither: Either[String, Response], filename: String): Either[String, Unit] = {
    responseEither.flatMap(response => {
      response.statusCode match {
        case OK | CREATED =>
          // scalastyle:off regex
          println(s"Published. ${getApiPublishPhrase(response.statusCode)} API. Response(${response.statusCode}): ${response.content}")
          // scalastyle:on regex
          Right(())
        case _            =>
          val errorMessage = s"Failed to publish '$filename'. Response(${response.statusCode}): ${response.content}"
          Left(errorMessage)
      }
    })
  }
}
