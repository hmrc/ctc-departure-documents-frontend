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
import forms.PreviousDocumentTypeFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{DocumentsNavigatorProvider, UserAnswersNavigator}
import pages.document.PreviousDocumentTypePage
import pages.external.TransitOperationDeclarationTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.PreviousDocumentTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.PreviousDocumentTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentTypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentsNavigatorProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: PreviousDocumentTypeFormProvider,
  service: PreviousDocumentTypesService,
  val controllerComponents: MessagesControllerComponents,
  view: PreviousDocumentTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "document.previousDocumentType"

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(TransitOperationDeclarationTypePage))
    .async {
      implicit request =>
        service.getPreviousDocumentTypes().map {
          previousDocumentTypeList =>
            val form = formProvider(prefix, previousDocumentTypeList)
            val preparedForm = request.userAnswers.get(PreviousDocumentTypePage(documentIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, lrn, previousDocumentTypeList.previousDocumentTypes, mode, request.arg, documentIndex))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(TransitOperationDeclarationTypePage))
    .async {
      implicit request =>
        service.getPreviousDocumentTypes().flatMap {
          previousDocumentTypeList =>
            val form = formProvider(prefix, previousDocumentTypeList)
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(BadRequest(view(formWithErrors, lrn, previousDocumentTypeList.previousDocumentTypes, mode, request.arg, documentIndex))),
                value => {
                  implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
                  PreviousDocumentTypePage(documentIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
                }
              )
        }
    }
}
