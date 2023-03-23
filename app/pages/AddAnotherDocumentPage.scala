package pages

import controllers.document.routes
import models.{Mode, UserAnswers}
import pages.sections.DocumentSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object AddAnotherDocumentPage extends QuestionPage[Boolean] {

  override def path: JsPath = DocumentSection.path \ toString

  override def toString: String = "addAnotherDocument"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AddAnotherDocumentController.onPageLoad(userAnswers.lrn, mode))
}
