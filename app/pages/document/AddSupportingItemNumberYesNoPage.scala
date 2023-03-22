package pages.document

import controllers.document.routes
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.DocumentDetailsSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object AddSupportingItemNumberYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = DocumentDetailsSection.path \ toString

  override def toString: String = "addSupportingItemNumberYesNo"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AddSupportingItemNumberYesNoController.onPageLoad(userAnswers.lrn, mode))
}
