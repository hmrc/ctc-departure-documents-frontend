package pages.document

import controllers.document.routes
import models.reference.Metric
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.ocumentDetailsSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object MetricPage extends QuestionPage[Metric] {

  override def path: JsPath = ocumentDetailsSection.path \ toString

  override def toString: String = "metric"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.MetricController.onPageLoad(userAnswers.lrn, mode))
}
