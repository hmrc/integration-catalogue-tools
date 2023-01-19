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

import java.io.File
import java.nio.file.Files
import scala.collection.JavaConverters._

import uk.gov.hmrc.integrationcataloguetools.utils.ExtractPublisherReference.Implicits

object CompareYamlFiles {

  def findApisToUnpublish(previousPath: String, updatedPath: String): Either[String, List[String]] = {
    validateInputs(previousPath, updatedPath) match {
      case Some(errorMessage) => Left(errorMessage)
      case None               => Right {
          val filenamesAndPublisherRefsFromBeforePath: Map[String, String] = getYamlFiles(previousPath)
            .map(fileName => (fileName.extractPublisherReference, fileName))
            .toMap

          val publisherRefsInAfterPath: Set[String] = getYamlFiles(updatedPath)
            .map(_.extractPublisherReference)
            .toSet

          getMissingFilenames(filenamesAndPublisherRefsFromBeforePath, publisherRefsInAfterPath)
        }
    }
  }

  // List of filenames from filenamesAndPublisherRefs whose publisherRefs (map keys) are not in the publisherRefs set
  private def getMissingFilenames(filenamesAndPublisherRefs: Map[String, String], publisherRefs: Set[String]): List[String] =
    filenamesAndPublisherRefs
      .filterNot(x => publisherRefs.contains(x._1))
      .values
      .toList
      .sorted

  private def validateInputs(beforePath: String, afterPath: String): Option[String] = {
    val beforeDirectory = new File(beforePath)
    val afterDirectory  = new File(afterPath)
    if (!beforeDirectory.exists || !beforeDirectory.isDirectory) {
      Some(s"Path is not a directory: $beforePath")
    } else if (!afterDirectory.exists || !afterDirectory.exists) {
      Some(s"Path is not a directory: $afterPath")
    } else None
  }

  private def getYamlFiles(path: String): List[String] = {
    Files.walk(new File(path).toPath)
      .iterator().asScala
      .map(_.getFileName.toString)
      .filter(_.endsWith(".yaml"))
      .toList
  }
}
