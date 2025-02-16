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

import models.TaskStatus.{Amended, Completed}
import play.api.libs.json.*

sealed trait SubmissionState {
  val asString: String

  def taskStatus: TaskStatus = this match {
    case SubmissionState.Amendment              => Amended
    case SubmissionState.GuaranteeAmendment     => Amended
    case SubmissionState.RejectedPendingChanges => Amended
    case _                                      => Completed
  }
}

object SubmissionState {

  case object NotSubmitted extends SubmissionState {
    override val asString: String = "notSubmitted"
  }

  case object Submitted extends SubmissionState {
    override val asString: String = "submitted"
  }

  case object RejectedPendingChanges extends SubmissionState {
    override val asString: String = "rejectedPendingChanges"
  }

  case object Amendment extends SubmissionState {
    override val asString: String = "amendment"
  }

  case object GuaranteeAmendment extends SubmissionState {
    override val asString: String = "guaranteeAmendment"
  }

  implicit val reads: Reads[SubmissionState] = Reads {
    case JsString(NotSubmitted.asString)           => JsSuccess(NotSubmitted)
    case JsString(Submitted.asString)              => JsSuccess(Submitted)
    case JsString(RejectedPendingChanges.asString) => JsSuccess(RejectedPendingChanges)
    case JsString(Amendment.asString)              => JsSuccess(Amendment)
    case JsString(GuaranteeAmendment.asString)     => JsSuccess(GuaranteeAmendment)
    case x                                         => JsError(s"Could not read $x as SubmissionState")
  }

  implicit val writes: Writes[SubmissionState] = Writes {
    state => JsString(state.asString)
  }

}
