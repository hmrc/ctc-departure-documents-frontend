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

import controllers.actions._

import javax.inject.Inject
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{DocumentsNavigatorProvider, UserAnswersNavigator}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.document.DocumentAnswersViewModel.DocumentAnswersViewModelProvider
import views.html.document.DocumentAnswersView

class DocumentAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: DocumentAnswersView,
  viewModelProvider: DocumentAnswersViewModelProvider,
  navigatorProvider: DocumentsNavigatorProvider
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val sections = viewModelProvider(request.userAnswers, mode, documentIndex).sections
      Ok(view(lrn, mode, documentIndex, sections))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
      Redirect(navigator.nextPage(request.userAnswers))
  }
}
