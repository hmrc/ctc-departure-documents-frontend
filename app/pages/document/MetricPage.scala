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

package pages.document

import controllers.document.routes
import models.reference.Metric
import models.{Index, Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.DocumentDetailsSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class MetricPage(documentIndex: Index) extends QuestionPage[Metric] {

  override def path: JsPath = DocumentDetailsSection(documentIndex).path \ toString

  override def toString: String = "metric"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.MetricController.onPageLoad(userAnswers.lrn, mode, documentIndex))

  override def cleanup(value: Option[Metric], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) => userAnswers.remove(QuantityPage(documentIndex))
      case None    => super.cleanup(value, userAnswers)
    }
}
