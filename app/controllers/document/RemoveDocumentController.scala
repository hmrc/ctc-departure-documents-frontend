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
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner, UpdateOps}
import forms.YesNoFormProvider
import models.journeyDomain.DocumentDomain
import models.requests.DataRequest
import models.{Index, LocalReferenceNumber}
import pages.document.{DocumentReferenceNumberPage, DocumentUuidPage, PreviousDocumentTypePage, TypePage}
import pages.sections.DocumentSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.RemoveDocumentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveDocumentController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveDocumentView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] = formProvider("document.removeDocument")

  private def addAnother(lrn: LocalReferenceNumber): Call =
    controllers.routes.AddAnotherDocumentController.onPageLoad(lrn)

  private def document(index: Index)(implicit request: DataRequest[_]): String =
    DocumentDomain.asString(
      index,
      request.userAnswers.get(TypePage(index)) orElse request.userAnswers.get(PreviousDocumentTypePage(index)),
      request.userAnswers.get(DocumentReferenceNumberPage(index))
    )

  def onPageLoad(lrn: LocalReferenceNumber, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, DocumentSection(documentIndex), addAnother(lrn)) {
      implicit request =>
        Ok(view(form, lrn, documentIndex, document(documentIndex)))
    }

  def onSubmit(lrn: LocalReferenceNumber, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, DocumentSection(documentIndex), addAnother(lrn))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, documentIndex, document(documentIndex)))),
            {
              case true =>
                DocumentSection(documentIndex)
                  .removeFromUserAnswers()
                  .removeDocumentFromItems(request.userAnswers.get(DocumentUuidPage(documentIndex)))
                  .updateTask()
                  .writeToSession(sessionRepository)
                  .buildCall(addAnother(lrn))
                  .updateItems(lrn)
                  .navigate
              case false =>
                Future.successful(Redirect(addAnother(lrn)))
            }
          )
    }
}
