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

package uk.gov.hmrc.integrationcataloguetools.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.integrationcataloguetools.utils.ExtractPublisherReference.Implicits

class ExtractPublisherReferenceSpec extends AnyWordSpec with Matchers {
  
  "extractPublisherReference" should {

    "find the number with just the number in the text" in {
      val result = "1234".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number with just four digits in the text" in {
      val result = "12345 6789".extractPublisherReference
      result shouldBe "6789"
    }

    "find the number with first four digits in the text" in {
      val result = "1234 5678".extractPublisherReference
      result shouldBe "1234"
    }

    "return an empty string if there are no four digit numbers" in {
      val result = "123".extractPublisherReference
      result shouldBe ""
    }

    "find the number with lowercase 'api' before the number" in {
      val result = "api1234".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number with uppercase 'API' before the number" in {
      val result = "API1234".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number with # between 'api' and the number" in {
      val result = "api#1234".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number with a space between 'api' and the number" in {
      val result = "api 1234".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number with text before and after the number" in {
      val result = "api-1234_name_of_api".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number with text before and after the number with no api in text" in {
      val result = "name_of_1234_1.0".extractPublisherReference
      result shouldBe "1234"
    }
  }
}
