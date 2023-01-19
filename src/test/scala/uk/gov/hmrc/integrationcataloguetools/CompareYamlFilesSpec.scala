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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CompareYamlFilesSpec extends AnyWordSpec with Matchers {

  trait Setup {
    val testResourcesPath = "src/test/resources"
    val topLevelPath      = s"$testResourcesPath/compareyamlfilesspec"
    val previousPath      = s"$topLevelPath/previous"
    val updatedPath       = s"$topLevelPath/updated"

    val previousFiles: Map[String, String] = Map(
      "1003" -> "API#1003_api-name-1.2.0.yaml",
      "1004" -> "API1004_Api_Name_1.5.0.yaml",
      "1005" -> "api-1005-some-api-name-1.1.0.yaml"
    )

    val updatedFilesPlatformRefs: Set[String] = Set(
      "1001",
      "1002",
      "1005",
      "1006"
    )

    val previousFilesMissingFromUpdatedPath = List(
      "API#1003_api-name-1.2.0.yaml",
      "API1004_Api_Name_1.5.0.yaml"
    )
  }

  "findApisToUnpublish" should {
    "return APIs to unpublish when comparing previous platform filenames against updated platform files" in new Setup {
      CompareYamlFiles.findApisToUnpublish(previousPath, updatedPath) shouldBe
        Right(previousFilesMissingFromUpdatedPath)
    }

    "return an error when the previous path is not a directory" in new Setup {
      CompareYamlFiles.findApisToUnpublish("bad-folder-name", updatedPath) shouldBe
        Left("Path is not a directory: bad-folder-name")
    }

    "return an error when the updated path is not a directory" in new Setup {
      CompareYamlFiles.findApisToUnpublish(previousPath, "bad-folder-name") shouldBe
        Left("Path is not a directory: bad-folder-name")
    }
  }

}
