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

import config.FrontendAppConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.requests.DataRequest
import models.{ConsignmentLevelDocuments, Index, LocalReferenceNumber, Mode}
import navigation.{DocumentNavigatorProvider, UserAnswersNavigator}
import pages.QuestionPage
import pages.document.{AttachToAllItemsPage, InferredAttachToAllItemsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.AttachToAllItemsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AttachToAllItemsController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentNavigatorProvider,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AttachToAllItemsView
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("document.attachToAllItems")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      if (ConsignmentLevelDocuments(request.userAnswers, documentIndex).cannotAddAnyMore) {
        redirect(mode, documentIndex, InferredAttachToAllItemsPage, value = false)
      } else {
        val preparedForm = request.userAnswers.get(AttachToAllItemsPage(documentIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Future.successful(Ok(view(preparedForm, lrn, mode, documentIndex)))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, documentIndex))),
          value => redirect(mode, documentIndex, AttachToAllItemsPage, value)
        )
  }

  private def redirect(
    mode: Mode,
    index: Index,
    page: Index => QuestionPage[Boolean],
    value: Boolean
  )(implicit request: DataRequest[_]): Future[Result] = {
    implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, index)
    page(index)
      .writeToUserAnswers(value)
      .updateTask()
      .writeToSession()
      .navigate()
  }
}
