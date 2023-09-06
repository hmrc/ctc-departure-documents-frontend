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
import forms.YesNoFormProvider
import models.reference.Document
import models.requests.SpecificDataRequestProvider2
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
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveDocumentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(documentType: Document): Form[Boolean] = formProvider("document.removeDocument", documentType)

  private type Request = SpecificDataRequestProvider2[Document, String]#SpecificDataRequest[_]

  private def documentType(implicit request: Request): Document          = request.arg._1
  private def documentReferenceNumber(implicit request: Request): String = request.arg._2

  private def addAnother(lrn: LocalReferenceNumber): Call =
    controllers.routes.AddAnotherDocumentController.onPageLoad(lrn)

  // TODO - create a view model for this controller. Ref number might not be populated for an in-progress document.
  def onPageLoad(lrn: LocalReferenceNumber, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, DocumentSection(documentIndex), addAnother(lrn))
    .andThen(getMandatoryPage.getFirst(TypePage(documentIndex), PreviousDocumentTypePage(documentIndex)))
    .andThen(getMandatoryPage.getSecond(DocumentReferenceNumberPage(documentIndex))) {
      implicit request =>
        Ok(view(form(documentType), lrn, documentIndex, documentType, documentReferenceNumber))
    }

  def onSubmit(lrn: LocalReferenceNumber, documentIndex: Index): Action[AnyContent] = actions
    .requireIndex(lrn, DocumentSection(documentIndex), addAnother(lrn))
    .andThen(getMandatoryPage(TypePage(documentIndex), PreviousDocumentTypePage(documentIndex)))
    .andThen(getMandatoryPage.getSecond(DocumentReferenceNumberPage(documentIndex)))
    .async {
      implicit request =>
        form(documentType)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, documentIndex, documentType, documentReferenceNumber))),
            {
              case true =>
                DocumentSection(documentIndex)
                  .removeFromUserAnswers()
                  .removeDocumentFromItems(request.userAnswers.get(DocumentUuidPage(documentIndex)))
                  .updateTask()
                  .writeToSession()
                  .navigateTo(addAnother(lrn))
              case false =>
                Future.successful(Redirect(addAnother(lrn)))
            }
          )
    }
}
