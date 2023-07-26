/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.document

import config.PhaseConfig
import controllers.actions._
import models.{Index, LocalReferenceNumber}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.document.DocumentAnswersViewModel.DocumentAnswersViewModelProvider
import views.html.document.DocumentAnswersView

import javax.inject.Inject

class DocumentAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: DocumentAnswersView,
  viewModelProvider: DocumentAnswersViewModelProvider
)(implicit phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val sections = viewModelProvider(request.userAnswers, documentIndex).sections
      Ok(view(lrn, documentIndex, sections))
  }

  def onSubmit(lrn: LocalReferenceNumber, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    _ => Redirect(controllers.routes.AddAnotherDocumentController.onPageLoad(lrn))
  }
}
