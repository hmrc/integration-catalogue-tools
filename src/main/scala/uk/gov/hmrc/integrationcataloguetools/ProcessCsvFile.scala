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

package uk.gov.hmrc.integrationcataloguetools

import java.io.FileReader

import net.liftweb.json.Serialization.writePretty
import net.liftweb.json.{DefaultFormats, Formats}

object ProcessCsvFile {

  private def writeToFile(filename: String, content: String): Unit = {
    import java.io.{BufferedWriter, File, FileWriter}

    val file = new File(filename)
    val bw   = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
  }

  def processApiCsv(inputFilename: String, outputFolder: String): Int = {
    val in = new FileReader(inputFilename)
    try {
      GenerateOpenApi
        .fromCsvToOasContent(in)
        .map {
          case (publisherReference, oasContent) =>
            val filename = s"$outputFolder/${publisherReference.value}.yaml"
            writeToFile(filename, oasContent)
        }.length
    } finally {
      in.close()
    }
  }

  def processFTCsv(inputFilename: String, outputFolder: String): Int = {
    val in = new FileReader(inputFilename)

    implicit val formats: Formats = DefaultFormats

    try {

      GenerateFileTransferJson
        .fromCsvToFileTransferRequest(in)
        .map {
          case (publisherReference, fileTransferRequest) =>
            val filename = s"$outputFolder/${publisherReference.value}.json"
            writeToFile(filename, writePretty(fileTransferRequest))
        }.length
    } finally {
      in.close()
    }
  }
}
