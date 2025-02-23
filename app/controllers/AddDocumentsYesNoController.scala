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

package controllers

import config.FrontendAppConfig
import controllers.actions.*
import forms.YesNoFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{DocumentsNavigatorProvider, UserAnswersNavigator}
import pages.AddDocumentsYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AddDocumentsYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddDocumentsYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: DocumentsNavigatorProvider,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddDocumentsYesNoView
)(implicit ec: ExecutionContext, frontendAppConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("addDocumentsYesNo")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddDocumentsYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, lrn, mode))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode))),
          value => {
            val navigator: UserAnswersNavigator = navigatorProvider(mode)
            AddDocumentsYesNoPage
              .writeToUserAnswers(value)
              .updateTask()
              .writeToSession(sessionRepository)
              .getNextPage {
                (page, userAnswers) =>
                  if (value) {
                    navigator.nextPage(userAnswers, Some(page))
                  } else {
                    routes.RedirectController.declarationSummary(lrn)
                  }
              }
              .updateItems(lrn)
              .navigate()
          }
        )
  }
}
