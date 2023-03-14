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

package models.reference

import base.SpecBase
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class PreviousDocumentTypeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "PreviousDocumentType" - {

    "must serialise" - {
      "when description defined" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val previousDocumentType = PreviousDocumentType(code, Some(description))
            Json.toJson(previousDocumentType) mustBe Json.parse(s"""
              |{
              |  "code": "$code",
              |  "description": "$description"
              |}
              |""".stripMargin)
        }
      }

      "when description undefined" in {
        forAll(Gen.alphaNumStr) {
          code =>
            val previousDocumentType = PreviousDocumentType(code, None)
            Json.toJson(previousDocumentType) mustBe Json.parse(s"""
              |{
              |  "code": "$code"
              |}
              |""".stripMargin)
        }
      }

    }

    "must deserialise" - {
      "when description defined" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val json = Json.parse(s"""
              |{
              |  "code": "$code",
              |  "description": "$description"
              |}
              |""".stripMargin)
            json.as[PreviousDocumentType] mustBe PreviousDocumentType(code, Some(description))
        }
      }

      "when description undefined" in {
        forAll(Gen.alphaNumStr) {
          code =>
            val json = Json.parse(s"""
              |{
              |  "code": "$code"
              |}
              |""".stripMargin)
            json.as[PreviousDocumentType] mustBe PreviousDocumentType(code, None)
        }
      }
    }

    "must convert to select item" in {
      forAll(Gen.alphaNumStr, Gen.option(Gen.alphaNumStr), arbitrary[Boolean]) {
        (code, description, selected) =>
          val previousDocumentType = PreviousDocumentType(code, description)
          previousDocumentType.toSelectItem(selected) mustBe SelectItem(Some(code), s"$previousDocumentType", selected)
      }
    }

    "must format as string" - {
      "when description defined and non-empty" in {
        forAll(Gen.alphaNumStr, nonEmptyString) {
          (code, description) =>
            val previousDocumentType = PreviousDocumentType(code, Some(description))
            previousDocumentType.toString mustBe s"($code) $description"
        }
      }

      "when description defined and empty" in {
        forAll(Gen.alphaNumStr) {
          code =>
            val previousDocumentType = PreviousDocumentType(code, Some(""))
            previousDocumentType.toString mustBe code
        }
      }

      "when description undefined" in {
        forAll(Gen.alphaNumStr) {
          code =>
            val previousDocumentType = PreviousDocumentType(code, None)
            previousDocumentType.toString mustBe code
        }
      }
    }
  }

}
