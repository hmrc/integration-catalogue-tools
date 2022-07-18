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

/*
 * Explanation of the three sets of test files
 * 
 * - legacy files (e.g. des)           --> one that remains in legacy folder (1001), two that are moved out of legacy (1002, 1003)
 * - old platform files (e.g. core-if) --> two that are deleted altogether (1004, 1005), one that has been updated (1006)
 * - new files for core-if             --> one that is new (1007), plus the ones matching legacy or old files
 * 
 * If files in legacy are also in the new files, they must be removed from legacy, and instead be the final list in core-if
 * If files in the old file lists are missing from the new files, they will be removed altogether and no longer in core-if
 * 
 * So the new files are: 1002, 1003, 1006, 1007
 * 
 * Note: having two files missing or matching allows checking of sorting
 */
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
      CompareYamlFiles.findMissingAndMatching(oldPlatformFolder, newPlatformFolder) shouldBe
        Right((oldPlatformFilesMissingFromNewPlatform, oldPlatformFilesMatchingNewPlatform))
    }

    "return missing and matching when comparing legacy platform filenames against new platform files" in new Setup {
      CompareYamlFiles.findMissingAndMatching(legacyPlatformFolder, newPlatformFolder, includeSubfolders = true) shouldBe
        Right((legacyPlatformFilesMissingFromNewPlatform, legacyPlatformFilesMatchingNewPlatform))
    }

    "return empty lists when the before path is not a directory" in new Setup {
      CompareYamlFiles.findMissingAndMatching("bad-folder-name", newPlatformFolder) shouldBe
        Left("Before path is not a directory")
    }

    "return empty lists when the after path is not a directory" in new Setup {
      CompareYamlFiles.findMissingAndMatching(oldPlatformFolder, "bad-folder-name") shouldBe
        Left("After path is not a directory")
    }
  }

}
