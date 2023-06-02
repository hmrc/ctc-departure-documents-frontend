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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import forms.AddAnotherFormProvider
import models.{LocalReferenceNumber, Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.AddAnotherDocumentViewModel
import viewModels.AddAnotherDocumentViewModel.AddAnotherDocumentViewModelProvider
import views.html.AddAnotherDocumentView

import javax.inject.Inject

class AddAnotherDocumentController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  viewModelProvider: AddAnotherDocumentViewModelProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddAnotherDocumentView
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val mode: Mode = NormalMode

  private def form(viewModel: AddAnotherDocumentViewModel): Form[Boolean] =
    formProvider(viewModel.prefix)

  def onPageLoad(lrn: LocalReferenceNumber): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers)

      viewModel.count match {
        case 0 => Redirect(routes.AddDocumentsYesNoController.onPageLoad(lrn, mode))
        case _ => Ok(view(form(viewModel), lrn, viewModel))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, lrn, viewModel)),
          {
            case true  => Redirect(controllers.document.routes.AttachToAllItemsController.onPageLoad(lrn, mode, viewModel.nextIndex))
            case false => Redirect(config.taskListUrl(lrn))
          }
        )
  }
}
