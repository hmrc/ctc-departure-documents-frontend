package pages.document

import controllers.document.routes
import models.reference.DocumentType
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.DocumentSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object TypePage extends QuestionPage[DocumentType] {

  override def path: JsPath = DocumentSection.path \ toString

  override def toString: String = "type"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.TypeController.onPageLoad(userAnswers.lrn, mode))
}
