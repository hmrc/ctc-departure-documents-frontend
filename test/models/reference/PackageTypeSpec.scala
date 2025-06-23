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
import config.FrontendAppConfig
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class PackageTypeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "PackageType" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val packageType = PackageType(code, description)
          Json.toJson(packageType) mustBe Json.parse(s"""
            |{
            |  "code": "$code",
            |  "description": "$description"
            |}
            |""".stripMargin)
      }
    }

    "must deserialise json from reference data service" - {
      "when phase-5" in {
        running(_.configure("feature-flags.phase-6-enabled" -> false)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
              (code, description) =>
                val packageType = PackageType(code, description)
                val json = Json.parse(s"""
                   |{
                   |  "code": "$code",
                   |  "description": "$description"
                   |}
                   |""".stripMargin)
                json.as[PackageType](PackageType.reads(config)) mustEqual packageType
            }
        }

      }
      "when phase-6" in {
        running(_.configure("feature-flags.phase-6-enabled" -> true)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
              (code, description) =>
                val packageType = PackageType(code, description)
                val json = Json.parse(s"""
                   |{
                   |  "key": "$code",
                   |  "value": "$description"
                   |}
                   |""".stripMargin)
                json.as[PackageType](PackageType.reads(config)) mustEqual packageType
            }
        }

      }

    }
    "when reading from mongo" in {
      val config = app.injector.instanceOf[FrontendAppConfig]
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val packageType = PackageType(code, description)
          Json
            .parse(s"""
                 |{
                 |  "code": "$code",
                 |  "description": "$description"
                 |}
                 |""".stripMargin)
            .as[PackageType](PackageType.reads(config)) mustEqual packageType
      }
    }

    "must convert to select item" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[Boolean]) {
        (code, description, selected) =>
          val packageType = PackageType(code, description)
          packageType.toSelectItem(selected) mustBe SelectItem(Some(code), s"$packageType", selected)
      }
    }

    "must format as string" in {
      forAll(Gen.alphaNumStr, nonEmptyString) {
        (code, description) =>
          val packageType = PackageType(code, description)
          packageType.toString mustBe s"($code) $description"
      }
    }
  }

}
