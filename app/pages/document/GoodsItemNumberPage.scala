package pages.document

import controllers.document.routes
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.DocumentSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object GoodsItemNumberPage extends QuestionPage[String] {

  override def path: JsPath = DocumentSection.path \ toString

  override def toString: String = "goodsItemNumber"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.GoodsItemNumberController.onPageLoad(userAnswers.lrn, mode))
}
