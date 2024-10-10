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

package controllers.document

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.DocumentReferenceNumberFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{DocumentNavigatorProvider, UserAnswersNavigator}
import pages.document.{DocumentReferenceNumberPage, DocumentUuidPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.document.DocumentReferenceNumberViewModel
import viewModels.document.DocumentReferenceNumberViewModel.DocumentReferenceNumberViewModelProvider
import views.html.document.DocumentReferenceNumberView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DocumentReferenceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  val sessionRepository: SessionRepository,
  navigatorProvider: DocumentNavigatorProvider,
  formProvider: DocumentReferenceNumberFormProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: DocumentReferenceNumberView,
  viewModelProvider: DocumentReferenceNumberViewModelProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: DocumentReferenceNumberViewModel): Form[String] =
    formProvider("document.documentReferenceNumber", viewModel.otherReferenceNumbers)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider.apply(request.userAnswers, documentIndex)
      val preparedForm = request.userAnswers.get(DocumentReferenceNumberPage(documentIndex)) match {
        case None        => form(viewModel)
        case Some(value) => form(viewModel).fill(value)
      }
      Ok(view(preparedForm, lrn, mode, documentIndex))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      val viewModel = viewModelProvider.apply(request.userAnswers, documentIndex)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, documentIndex))),
          value => {
            val navigator: UserAnswersNavigator = navigatorProvider(mode, documentIndex)
            DocumentReferenceNumberPage(documentIndex)
              .writeToUserAnswers(value)
              .appendValueIfNotPresent(DocumentUuidPage(documentIndex), UUID.randomUUID())
              .updateTask()
              .writeToSession(sessionRepository)
              .navigateWith(navigator)
          }
        )
  }
}
