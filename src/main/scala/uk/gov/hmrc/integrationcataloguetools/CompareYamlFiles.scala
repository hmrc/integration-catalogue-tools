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

import java.io.File
import uk.gov.hmrc.integrationcataloguetools.utils.ExtractPublisherReference.Implicits

object CompareYamlFiles {

  def findMissingAndMatching(beforePath: String, afterPath: String): (List[String], List[String]) = {
    if (inputsAreNotValid(beforePath, afterPath)) (List.empty[String], List.empty[String])
    else {
      val filenamesAndPublisherRefsFromBeforePath: Map[String, String] = getYamlFiles(afterPath)
        .map(fileName => (fileName.extractPublisherReference, fileName))
        .toMap

      val publisherRefsInAfterPath: Set[String] = getYamlFiles(beforePath)
        .map(_.extractPublisherReference)
        .toSet

      (
        getMissingFilenames(filenamesAndPublisherRefsFromBeforePath, publisherRefsInAfterPath),
        getMatchingFilenames(filenamesAndPublisherRefsFromBeforePath, publisherRefsInAfterPath)
      )
    }
  }

  def getMissingFilenames(filenamesAndPublisherRefsFromBeforePath: Map[String, String], publisherRefsInAfterPath: Set[String]): List[String] =
    filenamesAndPublisherRefsFromBeforePath
      .filterNot(x => publisherRefsInAfterPath.contains(x._1))
      .values
      .toList

  def getMatchingFilenames(filenamesAndPublisherRefsFromBeforePath: Map[String, String], publisherRefsInAfterPath: Set[String]): List[String] =
    publisherRefsInAfterPath
      .filter(x => filenamesAndPublisherRefsFromBeforePath.contains(x))
      .map(x => filenamesAndPublisherRefsFromBeforePath.getOrElse(x, "Something ugly wrong here"))
      .toList

  private def inputsAreNotValid(beforePath: String, afterPath: String): Boolean = {
    var result = false
    val beforeDirectory = new File(beforePath)
    if (!beforeDirectory.exists || !beforeDirectory.isDirectory) {
      println("Before path is not a directory")
      result = true
    }
    val afterDirectory = new File(afterPath)
    if (!afterDirectory.exists || !afterDirectory.exists) {
      println("After path is not a directory")
      result = true
    }
    result
  }

  private def getYamlFiles(beforePath: String) = {
    new File(beforePath).listFiles
      .filter(_.isFile)
      .map(_.getName)
      .filter(_.endsWith(".yaml"))
  }
}
