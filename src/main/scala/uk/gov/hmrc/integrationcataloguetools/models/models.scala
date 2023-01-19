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

package uk.gov.hmrc.integrationcataloguetools.models

case class PublisherReference(value: String) extends AnyVal

case class Platform(value: String) extends AnyVal
case class Status(value: String)   extends AnyVal

case class BasicApi(
    publisherReference: PublisherReference,
    platform: Platform,
    title: String,
    description: String,
    version: String,
    method: String,
    endpoint: String,
    parameters: List[String],
    status: Status,
    reviewedDate: String
  )

case class ContactInformation(name: String, emailAddress: String)

case class FileTransferPublishRequest(
    publisherReference: PublisherReference,
    fileTransferSpecificationVersion: String = "0.1",
    title: String,
    description: String,
    platformType: String,
    lastUpdated: String,
    reviewedDate: String,
    contact: ContactInformation,
    sourceSystem: List[String], // One or many
    targetSystem: List[String],
    transports: List[String],
    fileTransferPattern: String
  )
