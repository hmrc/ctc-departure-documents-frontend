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
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, Json}

class MetricSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "toString" - {
    "must format metric as string" in {
      val metric         = Metric("foo", "bar")
      val result         = metric.toString
      val expectedResult = "(foo) bar"
      result.mustBe(expectedResult)
    }
  }

  "format" - {
    "must serialise" in {
      forAll(nonEmptyString, nonEmptyString) {
        (code, description) =>
          val metric = Metric(code, description)
          val expectedResult = Json.parse(s"""
               |{
               |  "code" : "$code",
               |  "description" : "$description"
               |}
               |""".stripMargin)

          val result = Json.toJson(metric)
          result.mustBe(expectedResult)
      }
    }

    "must deserialise" - {
      "when json in expected shape" in {
        forAll(nonEmptyString, nonEmptyString) {
          (code, description) =>
            val json = Json.parse(s"""
                 |{
                 |  "code" : "$code",
                 |  "description" : "$description"
                 |}
                 |""".stripMargin)

            val expectedResult = Metric(code, description)
            val result         = json.validate[Metric]
            result.get.mustBe(expectedResult)
        }
      }
    }

    "must fail to deserialise" - {
      "when json in unexpected shape" in {
        forAll(nonEmptyString) {
          code =>
            val json = Json.parse(s"""
                 |{
                 |  "code" : "$code"
                 |}
                 |""".stripMargin)

            val result = json.validate[Metric]
            result.mustBe(a[JsError])
        }
      }
    }
  }
}
