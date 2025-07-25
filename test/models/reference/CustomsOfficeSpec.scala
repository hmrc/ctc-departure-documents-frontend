/*
 * Copyright 2024 HM Revenue & Customs
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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class CustomsOfficeSpec extends SpecBase with ScalaCheckPropertyChecks {

  "CustomsOffice" - {

    "must serialise" - {
      "when phone number defined" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
          (id, name, phoneNumber, countryId) =>
            val customsOffice = CustomsOffice(id, name, Some(phoneNumber), countryId)
            Json.toJson(customsOffice) mustEqual Json.parse(s"""
                |{
                |  "id": "$id",
                |  "name": "$name",
                |  "phoneNumber": "$phoneNumber",
                |  "countryId": "$countryId"
                |}
                |""".stripMargin)
        }
      }

      "when phone number undefined" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
          (id, name, countryId) =>
            val customsOffice = CustomsOffice(id, name, None, countryId)
            Json.toJson(customsOffice) mustEqual Json.parse(s"""
                |{
                |  "id": "$id",
                |  "name": "$name",
                |  "countryId": "$countryId"
                |}
                |""".stripMargin)
        }
      }
    }

    "must deserialise" - {
      "when phone number defined" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
          (id, name, phoneNumber, countryId) =>
            val customsOffice = CustomsOffice(id, name, Some(phoneNumber), countryId)
            Json
              .parse(s"""
                |{
                |  "id": "$id",
                |  "name": "$name",
                |  "phoneNumber": "$phoneNumber",
                |  "countryId": "$countryId"
                |}
                |""".stripMargin)
              .as[CustomsOffice] mustEqual customsOffice
        }
      }

      "when phone number undefined" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
          (id, name, countryId) =>
            val customsOffice = CustomsOffice(id, name, None, countryId)
            Json
              .parse(s"""
                |{
                |  "id": "$id",
                |  "name": "$name",
                |  "countryId": "$countryId"
                |}
                |""".stripMargin)
              .as[CustomsOffice] mustEqual customsOffice
        }
      }
    }

    "must convert to select item" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[Boolean]) {
        (id, name, countryId, selected) =>
          val customsOffice = CustomsOffice(id, name, None, countryId)
          customsOffice.toSelectItem(selected) mustEqual SelectItem(Some(id), s"$name ($id)", selected)
      }
    }

    "must format as string" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr, Gen.alphaNumStr) {
        (id, name, countryId) =>
          val customsOffice = CustomsOffice(id, name, None, countryId)
          customsOffice.toString mustEqual s"$name ($id)"
      }
    }
  }

}
