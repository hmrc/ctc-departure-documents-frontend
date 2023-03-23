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
import forms.PackageTypeFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{DocumentNavigatorProvider, UserAnswersNavigator}
import pages.document.PackageTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.PackagesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.PackageTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackageTypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentNavigatorProvider,
  actions: Actions,
  formProvider: PackageTypeFormProvider,
  service: PackagesService,
  val controllerComponents: MessagesControllerComponents,
  view: PackageTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "document.packageType"

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getPackageTypes().map {
        packageTypeList =>
          val form = formProvider(prefix, packageTypeList)
          val preparedForm = request.userAnswers.get(PackageTypePage(documentIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, lrn, packageTypeList.packageTypes, mode, documentIndex))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getPackageTypes().flatMap {
        packageTypeList =>
          val form = formProvider(prefix, packageTypeList)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, packageTypeList.packageTypes, mode, documentIndex))),
              value => {
                implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, documentIndex)
                PackageTypePage(documentIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
              }
            )
      }
  }
}
