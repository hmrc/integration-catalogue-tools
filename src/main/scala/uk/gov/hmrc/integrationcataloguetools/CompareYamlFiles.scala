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

import uk.gov.hmrc.integrationcataloguetools.utils.ExtractPublisherReference.Implicits

import java.io.File
import java.nio.file.Files
import scala.collection.JavaConverters._

object CompareYamlFiles {

  def findMissingAndMatching(beforePath: String, afterPath: String): (List[String], List[String]) = {
    if (inputsAreNotValid(beforePath, afterPath)) (List.empty[String], List.empty[String])
    else {
      val filenamesAndPublisherRefsFromBeforePath: Map[String, String] = getYamlFiles(beforePath)
        .map(fileName => (fileName.extractPublisherReference, fileName))
        .toMap

      val publisherRefsInAfterPath: Set[String] = getYamlFiles(afterPath)
        .map(_.extractPublisherReference)
        .toSet

      (
        getMissingFilenames(filenamesAndPublisherRefsFromBeforePath, publisherRefsInAfterPath),
        getMatchingFilenames(filenamesAndPublisherRefsFromBeforePath, publisherRefsInAfterPath)
      )
    }
  }

  /**
   *
   * @param filenamesAndPublisherRefsFromBeforePath
   * @param publisherRefsInAfterPath
   * @return List of filenames from filenamesAndPublisherRefsFromBeforePath that do not exist in publisherRefsInAfterPath
   */
  def getMissingFilenames(filenamesAndPublisherRefsFromBeforePath: Map[String, String], publisherRefsInAfterPath: Set[String]): List[String] =
    filenamesAndPublisherRefsFromBeforePath
      .filterNot(x => publisherRefsInAfterPath.contains(x._1))
      .values
      .toList
      .sorted

  /**
   *
   * @param filenamesAndPublisherRefsFromBeforePath
   * @param publisherRefsInAfterPath
   * @return List of filenames from filenamesAndPublisherRefsFromBeforePath that also exist in publisherRefsInAfterPath
   */
  def getMatchingFilenames(filenamesAndPublisherRefsFromBeforePath: Map[String, String], publisherRefsInAfterPath: Set[String]): List[String] =
    publisherRefsInAfterPath
      .filter(x => filenamesAndPublisherRefsFromBeforePath.contains(x))
      .map(x => filenamesAndPublisherRefsFromBeforePath.getOrElse(x, "Something ugly wrong here"))
      .toList
      .sorted

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

  private def getYamlFiles(path: String, isRecursive: Boolean = false): List[String] = {
    Files.walk(new File(path).toPath, isRecursive ? 2 : 1).iterator().asScala
      .map(_.getFileName.toString)
      .filter(_.endsWith(".yaml"))
      .toList
  }
}
