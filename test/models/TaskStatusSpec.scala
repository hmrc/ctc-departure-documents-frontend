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

import base.SpecBase
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}
import org.scalacheck.Arbitrary.arbitrary

class TaskStatusSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "isCompleted" - {
    "when completed" - {
      "must return true" in {
        val status = TaskStatus.Completed
        val result = status.isCompleted
        result.mustEqual(true)
      }
    }

    "when otherwise" - {
      "must return false" in {
        forAll(arbitrary[TaskStatus].retryUntil(_ != TaskStatus.Completed)) {
          status =>
            val result = status.isCompleted
            result.mustEqual(false)
        }
      }
    }
  }

  "isUnavailable" - {
    "when unavailable" - {
      "must return true" in {
        val status = TaskStatus.Unavailable
        val result = status.isUnavailable
        result.mustEqual(true)
      }
    }

    "when otherwise" - {
      "must return false" in {
        forAll(arbitrary[TaskStatus].retryUntil(_ != TaskStatus.Unavailable)) {
          status =>
            val result = status.isUnavailable
            result.mustEqual(false)
        }
      }
    }
  }

  "must serialise to json" - {
    "when completed" in {
      val result = Json.toJson[TaskStatus](TaskStatus.Completed)
      result mustEqual JsString("completed")
    }

    "when in progress" in {
      val result = Json.toJson[TaskStatus](TaskStatus.InProgress)
      result mustEqual JsString("in-progress")
    }

    "when not started" in {
      val result = Json.toJson[TaskStatus](TaskStatus.NotStarted)
      result mustEqual JsString("not-started")
    }

    "when cannot start yet" in {
      val result = Json.toJson[TaskStatus](TaskStatus.CannotStartYet)
      result mustEqual JsString("cannot-start-yet")
    }

    "when unavailable" in {
      val result = Json.toJson[TaskStatus](TaskStatus.Unavailable)
      result mustEqual JsString("unavailable")
    }

    "when error" in {
      val result = Json.toJson[TaskStatus](TaskStatus.Error)
      result mustEqual JsString("error")
    }

    "when amended" in {
      val result = Json.toJson[TaskStatus](TaskStatus.Amended)
      result mustEqual JsString("amended")
    }
  }

  "must deserialise from json" - {
    "when completed" in {
      val result = JsString("completed").as[TaskStatus]
      result mustEqual TaskStatus.Completed
    }

    "when in progress" in {
      val result = JsString("in-progress").as[TaskStatus]
      result mustEqual TaskStatus.InProgress
    }

    "when not started" in {
      val result = JsString("not-started").as[TaskStatus]
      result mustEqual TaskStatus.NotStarted
    }

    "when cannot start yet" in {
      val result = JsString("cannot-start-yet").as[TaskStatus]
      result mustEqual TaskStatus.CannotStartYet
    }

    "when unavailable" in {
      val result = JsString("unavailable").as[TaskStatus]
      result mustEqual TaskStatus.Unavailable
    }

    "when error" in {
      val result = JsString("error").as[TaskStatus]
      result mustEqual TaskStatus.Error
    }

    "when amended" in {
      val result = JsString("amended").as[TaskStatus]
      result mustEqual TaskStatus.Amended
    }

    "when something else" in {
      val result = JsString("foo").validate[TaskStatus]
      result mustBe a[JsError]
    }
  }
}
