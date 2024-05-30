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

import config.FrontendAppConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.DocumentFormProvider
import models.requests.SpecificDataRequestProvider1
import models.{ConsignmentLevelDocuments, Index, LocalReferenceNumber, Mode}
import navigation.{DocumentNavigatorProvider, UserAnswersNavigator}
import pages.document.{AttachToAllItemsPage, InferredAttachToAllItemsPage, TypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DocumentsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.TypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentNavigatorProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: DocumentFormProvider,
  service: DocumentsService,
  val controllerComponents: MessagesControllerComponents,
  view: TypeView
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "document.type"

  private type Request = SpecificDataRequestProvider1[Boolean]#SpecificDataRequest[_]

  private def consignmentLevelDocuments(documentIndex: Index)(implicit request: Request): ConsignmentLevelDocuments =
    ConsignmentLevelDocuments(request.userAnswers, documentIndex)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(AttachToAllItemsPage(documentIndex), InferredAttachToAllItemsPage(documentIndex)))
    .async {
      implicit request =>
        service.getDocuments(request.arg).map {
          documentList =>
            val form = formProvider(prefix, documentList, consignmentLevelDocuments(documentIndex), request.arg)
            val preparedForm = request.userAnswers.get(TypePage(documentIndex)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Ok(view(preparedForm, lrn, documentList.values, mode, documentIndex))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, documentIndex: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(AttachToAllItemsPage(documentIndex), InferredAttachToAllItemsPage(documentIndex)))
    .async {
      implicit request =>
        service.getDocuments(request.arg).flatMap {
          documentList =>
            val form = formProvider(prefix, documentList, consignmentLevelDocuments(documentIndex), request.arg)
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, documentList.values, mode, documentIndex))),
                value => {
                  implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, documentIndex)
                  TypePage(documentIndex).writeToUserAnswers(value).updateTask().writeToSession().navigate()
                }
              )
        }
    }
}
