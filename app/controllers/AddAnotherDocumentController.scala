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
import models.DeclarationType._
import models.requests.DataRequest
import models.{Index, LocalReferenceNumber, Mode, NormalMode}
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.AddAnotherDocumentViewModel
import viewModels.AddAnotherDocumentViewModel.AddAnotherDocumentViewModelProvider
import views.html.AddAnotherDocumentView

import javax.inject.Inject
import scala.concurrent.Future

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

  private def form(viewModel: AddAnotherDocumentViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)

      viewModel.count match {
        case 0 => redirectToNextPage(mode)
        case _ => Future.successful(Ok(view(form(viewModel), lrn, viewModel)))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, lrn, viewModel)),
          {
            case true  => Redirect(controllers.document.routes.TypeController.onPageLoad(lrn, NormalMode, viewModel.nextIndex))
            case false => Redirect(config.taskListUrl(lrn))
          }
        )
  }

  private def redirectToNextPage(mode: Mode)(implicit request: DataRequest[_]): Future[Result] = {
    val isOfficeOfDepartureGB = request.userAnswers.get(TransitOperationOfficeOfDeparturePage).map(_.isInGB)
    val declarationType       = request.userAnswers.get(TransitOperationDeclarationTypePage)

    (isOfficeOfDepartureGB, declarationType) match {
      case (Some(true), Some(T2) | Some(T2F)) =>
        Future.successful(Redirect(controllers.document.routes.PreviousDocumentTypeController.onPageLoad(request.userAnswers.lrn, mode, Index(0))))
      case _ => Future.successful(Redirect(controllers.document.routes.TypeController.onPageLoad(request.userAnswers.lrn, mode, Index(0))))
    }
  }
}