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
        result.mustBe(true)
      }
    }

    "when otherwise" - {
      "must return false" in {
        forAll(arbitrary[TaskStatus].retryUntil(_ != TaskStatus.Completed)) {
          status =>
            val result = status.isCompleted
            result.mustBe(false)
        }
      }
    }
  }

  "isUnavailable" - {
    "when unavailable" - {
      "must return true" in {
        val status = TaskStatus.Unavailable
        val result = status.isUnavailable
        result.mustBe(true)
      }
    }

    "when otherwise" - {
      "must return false" in {
        forAll(arbitrary[TaskStatus].retryUntil(_ != TaskStatus.Unavailable)) {
          status =>
            val result = status.isUnavailable
            result.mustBe(false)
        }
      }
    }
  }

  "must serialise to json" - {
    "when completed" in {
      val result = Json.toJson[TaskStatus](TaskStatus.Completed)
      result mustBe JsString("completed")
    }

    "when in progress" in {
      val result = Json.toJson[TaskStatus](TaskStatus.InProgress)
      result mustBe JsString("in-progress")
    }

    "when not started" in {
      val result = Json.toJson[TaskStatus](TaskStatus.NotStarted)
      result mustBe JsString("not-started")
    }

    "when cannot start yet" in {
      val result = Json.toJson[TaskStatus](TaskStatus.CannotStartYet)
      result mustBe JsString("cannot-start-yet")
    }

    "when unavailable" in {
      val result = Json.toJson[TaskStatus](TaskStatus.Unavailable)
      result mustBe JsString("unavailable")
    }

    "when error" in {
      val result = Json.toJson[TaskStatus](TaskStatus.Error)
      result mustBe JsString("error")
    }

    "when amended" in {
      val result = Json.toJson[TaskStatus](TaskStatus.Amended)
      result mustBe JsString("amended")
    }
  }

  "must deserialise from json" - {
    "when completed" in {
      val result = JsString("completed").as[TaskStatus]
      result mustBe TaskStatus.Completed
    }

    "when in progress" in {
      val result = JsString("in-progress").as[TaskStatus]
      result mustBe TaskStatus.InProgress
    }

    "when not started" in {
      val result = JsString("not-started").as[TaskStatus]
      result mustBe TaskStatus.NotStarted
    }

    "when cannot start yet" in {
      val result = JsString("cannot-start-yet").as[TaskStatus]
      result mustBe TaskStatus.CannotStartYet
    }

    "when unavailable" in {
      val result = JsString("unavailable").as[TaskStatus]
      result mustBe TaskStatus.Unavailable
    }

    "when error" in {
      val result = JsString("error").as[TaskStatus]
      result mustBe TaskStatus.Error
    }

    "when amended" in {
      val result = JsString("amended").as[TaskStatus]
      result mustBe TaskStatus.Amended
    }

    "when something else" in {
      val result = JsString("foo").validate[TaskStatus]
      result mustBe a[JsError]
    }
  }
}
