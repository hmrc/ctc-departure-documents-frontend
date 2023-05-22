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
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.BigDecimalFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{DocumentNavigatorProvider, UserAnswersNavigator}
import pages.document.{MetricPage, QuantityPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.QuantityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QuantityController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentNavigatorProvider,
  formProvider: BigDecimalFormProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: QuantityView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("document.quantity")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(MetricPage(index))) {
      implicit request =>
        val preparedForm = request.userAnswers.get(QuantityPage(index)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, lrn, mode, index, request.arg))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(MetricPage(index)))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, index, request.arg))),
            value => {
              implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, index)
              QuantityPage(index).writeToUserAnswers(value).updateTask().writeToSession().navigate()
            }
          )
    }
}
