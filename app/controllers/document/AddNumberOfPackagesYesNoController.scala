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
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{DocumentNavigatorProvider, UserAnswersNavigator}
import pages.document.{AddNumberOfPackagesYesNoPage, PackageTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.AddNumberOfPackagesYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddNumberOfPackagesYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentNavigatorProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddNumberOfPackagesYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("document.addNumberOfPackagesYesNo")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(PackageTypePage(documentIndex))) {
      implicit request =>
        val preparedForm = request.userAnswers.get(AddNumberOfPackagesYesNoPage(documentIndex)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, lrn, mode, documentIndex, request.arg.toString))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(PackageTypePage(documentIndex)))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, documentIndex, request.arg.toString))),
            value => {
              implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, documentIndex)
              AddNumberOfPackagesYesNoPage(documentIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
            }
          )
    }
}
