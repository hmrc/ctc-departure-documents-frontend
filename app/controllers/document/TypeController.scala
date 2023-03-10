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
import forms.DocumentTypeFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{DocumentsNavigatorProvider, UserAnswersNavigator}
import pages.document.TypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DocumentTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.TypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentsNavigatorProvider,
  actions: Actions,
  formProvider: DocumentTypeFormProvider,
  service: DocumentTypesService,
  val controllerComponents: MessagesControllerComponents,
  view: TypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "document.type"

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getDocumentTypes().map {
        documentTypeList =>
          val form = formProvider(prefix, documentTypeList)
          val preparedForm = request.userAnswers.get(TypePage(documentIndex)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, lrn, documentTypeList.documentTypes, mode, documentIndex))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getDocumentTypes().flatMap {
        documentTypeList =>
          val form = formProvider(prefix, documentTypeList)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, documentTypeList.documentTypes, mode, documentIndex))),
              value => {
                implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
                TypePage(documentIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
              }
            )
      }
  }
}
