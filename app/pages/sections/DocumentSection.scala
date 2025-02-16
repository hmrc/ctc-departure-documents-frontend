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

package pages.sections

import controllers.document.routes
import models.{Index, Mode, UserAnswers}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Call

case class DocumentSection(index: Index) extends Section[JsObject] {

  override def path: JsPath = DocumentsSection.path \ index.position

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.DocumentAnswersController.onPageLoad(userAnswers.lrn, index))
}
