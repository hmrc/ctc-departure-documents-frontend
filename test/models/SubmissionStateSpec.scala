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

package models

import generators.Generators
import models.SubmissionState.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsError, JsString, Json}

class SubmissionStateSpec extends AnyFreeSpec with Generators with Matchers with EitherValues {

  "submissionState" - {

    "must deserialise" - {
      "when json in expected shape" in {
        forAll(arbitrary[SubmissionState]) {
          state =>
            val result = JsString(state.asString).validate[SubmissionState]
            result.get.mustEqual(state)
        }
      }
    }

    "must fail to deserialise" - {
      "when json in unexpected shape" in {
        forAll(nonEmptyString) {
          value =>
            val result = JsString(value).validate[SubmissionState]
            result.mustBe(a[JsError])
        }
      }
    }

    "must serialise" in {
      forAll(arbitrary[SubmissionState]) {
        state =>
          Json.toJson(state) mustEqual JsString(state.asString)
      }
    }

    "taskStatus" - {

      "when status is non amendment" in {
        forAll(
          arbitrary[SubmissionState](arbitraryNonAmendmentSubmissionState)
        ) {
          state =>
            state.taskStatus mustEqual TaskStatus.Completed
        }
      }

      "when status is amendment" in {
        forAll(
          arbitrary[SubmissionState](arbitraryAmendmentSubmissionState)
        ) {
          state =>
            state.taskStatus mustEqual TaskStatus.Amended
        }
      }
    }
  }
}
