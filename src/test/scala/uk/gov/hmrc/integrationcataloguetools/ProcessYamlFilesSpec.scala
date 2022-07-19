/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.file.{Files, Path}
import java.util.Comparator
import scala.io.Source

class ProcessYamlFilesSpec extends AnyWordSpec with Matchers {

  trait Setup {
    val testResourcesPath = "src/test/resources"
    val topLevelPath = "processyamlfilesspec"
    val inputFolder = s"$topLevelPath/input"
    val expectedOutputFolder = s"$topLevelPath/expected"
    val outputFolder = s"$topLevelPath/output"

    def checkFileContents(fileName: String) = {
      val source = Source.fromFile(s"$testResourcesPath/$outputFolder/$fileName")
      val output = try source.mkString finally source.close()
      val expected = Source.fromResource(s"$expectedOutputFolder/$fileName").mkString

      output shouldBe expected
    }

    def cleanUpOutputFolder() = {
      val outputPathFile = new File(s"$testResourcesPath/$outputFolder")
      if (outputPathFile.exists) {
        Files.walk(outputPathFile.toPath)
          .sorted(Comparator.reverseOrder[Path]())
          .forEach((x: Path) => Files.delete(x))
      }
    }
  }

  "addMetadata" should {
    "write yaml files correctly to the output folder and return the number of files processed" in new Setup {
      cleanUpOutputFolder()

      val result = ProcessYamlFiles.addMetadata(s"$testResourcesPath/$inputFolder", "CORE_IF", "2022-04-22T20:27:05Z", s"$testResourcesPath/$outputFolder")

      result shouldBe Right(5)
      // The first of these files has a UTF-8 Byte Order Mark, and CRLF line endings
      checkFileContents("API#1488_ Display_Trust_or_Estate_4.1.0.yaml")
      checkFileContents("API#1758_Get_Breathing_Space_Status-1.3.0.yaml")
      checkFileContents("API1562_Store_Document_1.6.0.yaml")
      checkFileContents("api-1808-create-first-stage-appeal-1.0.0.yaml")
      checkFileContents("get_employer_API1560_0.1.0.yaml")
    }

    "return an error message if the input path is not a directory" in new Setup {
      cleanUpOutputFolder()
      val result = ProcessYamlFiles.addMetadata("bad-folder-name", "CORE_IF", "2022-04-22T20:27:05Z", s"$testResourcesPath/$outputFolder")
      result shouldBe Left("Input path is not a directory")
    }

    "return an error message if the output path is not empty" in new Setup {
      cleanUpOutputFolder()
      val result = ProcessYamlFiles.addMetadata(s"$testResourcesPath/$inputFolder", "CORE_IF", "2022-04-22T20:27:05Z", testResourcesPath)
      result shouldBe Left("Output path is not empty")
    }

    "return an error message if the reviewed date is not valid ISO-8601" in new Setup {
      cleanUpOutputFolder()
      val result = ProcessYamlFiles.addMetadata(s"$testResourcesPath/$inputFolder", "CORE_IF", "2022-04-22", s"$testResourcesPath/$outputFolder")
      result shouldBe Left("Reviewed date is not in ISO-8601 format")
    }
  }

  "validateReviewedDate" should {
    val errorMessage = "Reviewed date is not in ISO-8601 format"
    "succeed if reviewedDate is valid" in {
      ProcessYamlFiles.validateReviewedDate("2022-07-13T13:21:00Z") shouldBe None
    }
    "succeed if reviewedDate has 000 milliseconds" in {
      ProcessYamlFiles.validateReviewedDate("2022-07-13T13:21:00.000Z") shouldBe None
    }
    "succeed if reviewedDate has 123 milliseconds" in {
      ProcessYamlFiles.validateReviewedDate("2022-07-13T13:21:00.123Z") shouldBe None
    }
    "fail if reviewedDate does not have a timezone" in {
      ProcessYamlFiles.validateReviewedDate("2022-07-13T13:21:00") shouldBe Some(errorMessage)
    }
    "fail if reviewedDate does not have seconds" in {
      ProcessYamlFiles.validateReviewedDate("2022-07-13T13:21") shouldBe Some(errorMessage)
    }
    "fail if reviewedDate has invalid date" in {
      ProcessYamlFiles.validateReviewedDate("07-15-2022T13:21") shouldBe Some(errorMessage)
    }

  }

  "insertXIntegrationCatalogue" should {
    "insert x-integration-catalogue section" in {
      val input = List(
        "openapi: 3.0.3",
        "info:",
        "  title: API#1758 Get Breathing Space",
        "servers:",
        "  - url: https://{hostname}:{port}"
      )
      val expectedOutput = List(
        "openapi: 3.0.3",
        "info:",
        "  title: API#1758 Get Breathing Space",
        "  x-integration-catalogue:",
        "    reviewed-date: 2022-07-13T13:21:00Z",
        "    platform: PLAT",
        "    publisher-reference: 1758",
        "servers:",
        "  - url: https://{hostname}:{port}"
      )

      val result = ProcessYamlFiles.insertXIntegrationCatalogue(input, "PLAT", "2022-07-13T13:21:00Z", "1758")

      result shouldBe expectedOutput
    }

    "insert x-integration-catalogue section when info is the last section" in {
      val input = List(
        "openapi: 3.0.3",
        "info:",
        "  title: API#1758 Get Breathing Space"
      )
      val expectedOutput = List(
        "openapi: 3.0.3",
        "info:",
        "  title: API#1758 Get Breathing Space",
        "  x-integration-catalogue:",
        "    reviewed-date: 2022-07-13T13:21:00Z",
        "    platform: PLAT",
        "    publisher-reference: 1758"
      )

      val result = ProcessYamlFiles.insertXIntegrationCatalogue(input, "PLAT", "2022-07-13T13:21:00Z", "1758")

      result shouldBe expectedOutput
    }

    "not insert x-integration-catalogue section when input does not have info" in {
      val input = List(
        "openapi: 3.0.3",
        "servers:",
        "  - url: https://{hostname}:{port}"
      )

      val result = ProcessYamlFiles.insertXIntegrationCatalogue(input, "PLAT", "2022-07-13T13:21:00Z", "1758")

      result shouldBe input
    }

    "not insert x-integration-catalogue section if it already exists" in {
      val input = List(
        "openapi: 3.0.3",
        "info:",
        "  title: API#1758 Get Breathing Space",
        "  x-integration-catalogue:",
        "    reviewed-date: 2020-01-22T13:21:00Z",
        "    platform: CORE_IF",
        "    publisher-reference: 1758",
        "servers:",
        "  - url: https://{hostname}:{port}"
      )

      val result = ProcessYamlFiles.insertXIntegrationCatalogue(input, "PLAT", "2022-07-13T13:21:00Z", "1234")

      result shouldBe input
    }
  }
}
