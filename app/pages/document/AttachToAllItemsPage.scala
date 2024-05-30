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
import models.{Index, Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.{DocumentDetailsSection, DocumentSection}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

abstract class BaseAttachToAllItemsPage(documentIndex: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = DocumentSection(documentIndex).path \ toString

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AttachToAllItemsController.onPageLoad(userAnswers.lrn, mode, documentIndex))

  def cleanup(userAnswers: UserAnswers): Try[UserAnswers]

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = value match {
    case Some(_) =>
      cleanup(userAnswers)
        .flatMap(_.remove(TypePage(documentIndex)))
        .flatMap(_.remove(PreviousDocumentTypePage(documentIndex)))
        .flatMap(_.remove(DocumentDetailsSection(documentIndex)))
    case _ =>
      super.cleanup(value, userAnswers)
  }
}

case class AttachToAllItemsPage(documentIndex: Index) extends BaseAttachToAllItemsPage(documentIndex) {
  override def toString: String = "attachToAllItems"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(InferredAttachToAllItemsPage(documentIndex))
}

case class InferredAttachToAllItemsPage(documentIndex: Index) extends BaseAttachToAllItemsPage(documentIndex) {
  override def toString: String = "inferredAttachToAllItems"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(AttachToAllItemsPage(documentIndex))
}
