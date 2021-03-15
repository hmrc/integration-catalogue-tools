package uk.gov.hmrc.integrationcataloguetools.models

import java.time.LocalDateTime

case class PublisherReference(value: String) extends AnyVal

case class Platform(value: String) extends AnyVal

case class BasicApi(
  publisherReference: PublisherReference,
  platform: Platform,
  title: String,
  description: String,
  version: String,
  method: String,
  endpoint: String)

case class ContactInformation(name: String, emailAddress: String)

case class FileTransferPublishRequest(
    publisherReference: PublisherReference,
    fileTransferSpecificationVersion: String = "0.1",
    title: String,
    description: String,
    platformType: String,
    lastUpdated: String,
    contact: ContactInformation,
    sourceSystem: List[String], // One or many
    targetSystem: List[String],
    fileTransferPattern: String)
