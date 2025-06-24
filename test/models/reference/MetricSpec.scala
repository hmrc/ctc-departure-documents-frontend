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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, Json}
import play.api.test.Helpers.running

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
    "must serialise" - {
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
          result.mustEqual(expectedResult)
      }
    }

    "must deserialise" - {
      "when phase-6" - {
        "when json in expected shape" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(nonEmptyString, nonEmptyString) {
                (code, description) =>
                  val metric = Metric(code, description)
                  val json = Json.parse(s"""
                       |{
                       |  "key" : "$code",
                       |  "value" : "$description"
                       |}
                       |""".stripMargin)

                  json.as[Metric](Metric.reads(config)) mustEqual metric
              }

          }
        }
      }
      "when phase-5" - {
        "when json in expected shape" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(nonEmptyString, nonEmptyString) {
                (code, description) =>
                  val metric = Metric(code, description)
                  val json = Json.parse(s"""
                       |{
                       |  "code" : "$code",
                       |  "description" : "$description"
                       |}
                       |""".stripMargin)

                  json.as[Metric](Metric.reads(config)) mustEqual metric
              }

          }
        }
      }

    }

    "when reading from mongo" in {
      forAll(nonEmptyString, nonEmptyString) {
        (code, description) =>
          val metric = Metric(code, description)
          Json
            .parse(s"""
                 |{
                 |  "code": "$code",
                 |  "description": "$description"
                 |}
                 |""".stripMargin)
            .as[Metric] mustEqual metric
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
