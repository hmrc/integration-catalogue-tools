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

class CompareYamlFilesSpec extends AnyWordSpec with Matchers {

  trait Setup {
    val testResourcesPath = "src/test/resources"
    val topLevelPath = s"$testResourcesPath/compareyamlfilesspec"
    val legacyPlatformFolder = s"$topLevelPath/legacyplatform"
    val newPlatformFolder = s"$topLevelPath/newplatform"
    val oldPlatformFolder = s"$topLevelPath/oldplatform"

    val oldPlatformFiles: Map[String, String] = Map(
      "1004" -> "API#1004_api-name-1.2.0.yaml",
      "1005" -> "API1005_Api_Name_1.5.0.yaml",
      "1006" -> "api-1006-some-api-name-1.1.0.yaml"
    )

    val newPlatformPublisherRefs: Set[String] = Set(
      "1002",
      "1003",
      "1006",
      "1007"
    )

    val legacyPlatformFiles: Map[String, String] = Map(
      "1001" -> "1001.yaml",
      "1002" -> "API#1002_some_api_name-1.1.0.yaml",
      "1003" -> "1003.yaml"
    )

    val oldPlatformFilesMissingFromNewPlatform = List(
      "API#1004_api-name-1.2.0.yaml",
      "API1005_Api_Name_1.5.0.yaml"
    )

    val legacyPlatformFilesMissingFromNewPlatform = List("1001.yaml")

    val oldPlatformFilesMatchingNewPlatform = List("api-1006-some-api-name-1.1.0.yaml")

    val legacyPlatformFilesMatchingNewPlatform = List(
      "1003.yaml",
      "API#1002_some_api_name-1.1.0.yaml"
    )

  }

  "getMissingFilenames" should {
    "return the missing files when comparing old platform files against the new platform files" in new Setup {
      CompareYamlFiles.getMissingFilenames(oldPlatformFiles, newPlatformPublisherRefs) shouldBe oldPlatformFilesMissingFromNewPlatform
    }

    "return the missing files when comparing legacy platform files against the new platform files" in new Setup {
      CompareYamlFiles.getMissingFilenames(legacyPlatformFiles, newPlatformPublisherRefs) shouldBe legacyPlatformFilesMissingFromNewPlatform
    }
  }

  "getMatchingFilenames" should {
    "return the matching files when comparing old platform files against the new platform files" in new Setup {
      CompareYamlFiles.getMatchingFilenames(oldPlatformFiles, newPlatformPublisherRefs) shouldBe oldPlatformFilesMatchingNewPlatform
    }

    "return the matching files when comparing legacy platform files against the new platform files" in new Setup {
      CompareYamlFiles.getMatchingFilenames(legacyPlatformFiles, newPlatformPublisherRefs) shouldBe legacyPlatformFilesMatchingNewPlatform
    }
  }

  "findMissingAndMatching" should {
    "return missing and matching when comparing old platform filenames against new platform files" in new Setup {
      CompareYamlFiles.findMissingAndMatching(oldPlatformFolder, includeSubfolders = false, newPlatformFolder) shouldBe
        (oldPlatformFilesMissingFromNewPlatform, oldPlatformFilesMatchingNewPlatform)
    }

    "return missing and matching when comparing legacy platform filenames against new platform files" in new Setup {
      CompareYamlFiles.findMissingAndMatching(legacyPlatformFolder, includeSubfolders = true, newPlatformFolder) shouldBe
        (legacyPlatformFilesMissingFromNewPlatform, legacyPlatformFilesMatchingNewPlatform)
    }

    "return empty lists when the before path is not a directory" in new Setup {
      CompareYamlFiles.findMissingAndMatching("bad-folder-name", includeSubfolders = false, newPlatformFolder) shouldBe
        (List.empty, List.empty)
    }

    "return empty lists when the after path is not a directory" in new Setup {
      CompareYamlFiles.findMissingAndMatching("oldPlatformFolder", includeSubfolders = false, "bad-folder-name") shouldBe
        (List.empty, List.empty)
    }
  }

}

/*
 * legacy platform          --> one that remains legacy (1001), two that are moved to destination platform (1002, 1003)
 * destination platform old --> two that are deleted altogether (1004, 1005), one that's updated (1006)
 * destination platform new --> one that's new (1007), so contents of new = 1002, 1003, 1006, 1007
 *
 * TOTAL files = 7
 *
 * Tests:
 * - when running legacy compared to new, the result is: missing: [1001]; matching: [1002, 1003] >> we will delete matching files
 * - when running old compared to new, the result is: missing: [1004, 1005]; matching: [1006] >> we will delete missing files
 */
