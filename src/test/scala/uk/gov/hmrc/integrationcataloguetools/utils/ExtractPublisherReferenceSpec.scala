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

package uk.gov.hmrc.integrationcataloguetools.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.integrationcataloguetools.utils.ExtractPublisherReference.Implicits

class ExtractPublisherReferenceSpec extends AnyWordSpec with Matchers {
  
  "extractPublisherReference" should {
    "find the number in a simple name with lowercase API" in {
      val result = "api1234".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number in a simple name with uppercase API" in {
      val result = "API1234".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number in a name with # between 'api' and the number" in {
      val result = "api#1234".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number in a name with a space between 'api' and the number" in {
      val result = "api 1234".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number in a name with random text after the number" in {
      val result = "api-1234_name_of_api".extractPublisherReference
      result shouldBe "1234"
    }

    "find the number in with random text before and after the number" in {
      val result = "name_of_api_api1234_1.0".extractPublisherReference
      result shouldBe "1234"
    }
  }
}
