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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.Actions
import models.{Index, LocalReferenceNumber, NormalMode}
import navigation.DocumentsNavigatorProvider
import pages.document.InferredAttachToAllItemsPage
import pages.sections.DocumentsSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class RedirectController @Inject() (
  override val messagesApi: MessagesApi,
  navigatorProvider: DocumentsNavigatorProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def redirect(lrn: LocalReferenceNumber): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      Redirect(navigatorProvider.apply(NormalMode).nextPage(request.userAnswers, None))
  }

  def mandatoryPrevious(lrn: LocalReferenceNumber): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      val numberOfDocuments = request.userAnswers.getArraySize(DocumentsSection)
      val nextIndex         = Index(numberOfDocuments)
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(InferredAttachToAllItemsPage(nextIndex), false))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.document.routes.PreviousDocumentTypeController.onPageLoad(lrn, NormalMode, nextIndex))
  }

  def declarationSummary(lrn: LocalReferenceNumber): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      Redirect(config.taskListUrl(lrn))
  }
}
