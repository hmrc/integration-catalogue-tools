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
      if(outputPathFile.exists){
        Files.walk(outputPathFile.toPath)
          .sorted(Comparator.reverseOrder[Path]())
          .forEach((x: Path) => Files.delete(x))
      }
    }
  }

  "addMetadata" should {
    "write yaml files correctly to the output folder" in new Setup {
      cleanUpOutputFolder()

      ProcessYamlFiles.addMetadata(s"$testResourcesPath/$inputFolder", "CORE_IF", "2022-04-22T20:27:05Z", s"$testResourcesPath/$outputFolder")

      checkFileContents("API#1758_Get_Breathing_Space_Status-1.3.0.yaml")
      checkFileContents("API1562_Store_Document_1.6.0.yaml")
      checkFileContents("api-1808-create-first-stage-appeal-1.0.0.yaml")
      checkFileContents("get_employer_API1560_0.1.0.yaml")

    }
  }

  "insertXIntegrationCatalogue" should {
    "insert x-integration-catalogue section" in {
      val input =
        """openapi: 3.0.3
          |info:
          |  title: API#1758 Get Breathing Space
          |servers:
          |  - url: https://{hostname}:{port}""".stripMargin
      val expectedOutput =
        """openapi: 3.0.3
          |info:
          |  title: API#1758 Get Breathing Space
          |  x-integration-catalogue:
          |    reviewed-date: 2022-07-13T13:21:00Z
          |    platform: PLAT
          |    publisher-reference: 1758
          |servers:
          |  - url: https://{hostname}:{port}""".stripMargin

      val result = ProcessYamlFiles.insertXIntegrationCatalogue(input, "PLAT", "2022-07-13T13:21:00Z", "1758")

      result shouldBe expectedOutput
    }

    "insert x-integration-catalogue section when info is the last section" in {
      val input =
        """openapi: 3.0.3
          |info:
          |  title: API#1758 Get Breathing Space""".stripMargin
      val expectedOutput =
        """openapi: 3.0.3
          |info:
          |  title: API#1758 Get Breathing Space
          |  x-integration-catalogue:
          |    reviewed-date: 2022-07-13T13:21:00Z
          |    platform: PLAT
          |    publisher-reference: 1758""".stripMargin

      val result = ProcessYamlFiles.insertXIntegrationCatalogue(input, "PLAT", "2022-07-13T13:21:00Z", "1758")

      result shouldBe expectedOutput
    }

    "not insert x-integration-catalogue section when input does not have info" in {
      val input =
        """openapi: 3.0.3
          |servers:
          |  - url: https://{hostname}:{port}""".stripMargin
      val expectedOutput =
        """openapi: 3.0.3
          |servers:
          |  - url: https://{hostname}:{port}""".stripMargin

      val result = ProcessYamlFiles.insertXIntegrationCatalogue(input, "PLAT", "2022-07-13T13:21:00Z", "1758")

      result shouldBe expectedOutput
    }

    "not insert x-integration-catalogue section if it already exists" in {
      val input =
        """openapi: 3.0.3
          |info:
          |  title: API#1758 Get Breathing Space
          |  x-integration-catalogue:
          |    reviewed-date: 2020-01-22T13:21:00Z
          |    platform: CORE_IF
          |    publisher-reference: 1758
          |servers:
          |  - url: https://{hostname}:{port}""".stripMargin

      val result = ProcessYamlFiles.insertXIntegrationCatalogue(input, "PLAT", "2022-07-13T13:21:00Z", "1234")

      result shouldBe input
    }
  }
}
