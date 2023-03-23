package controllers.document

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.MetricFormProvider
import models.{Mode, LocalReferenceNumber}
import navigation.UserAnswersNavigator
import pages.document.MetricPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.MetricsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.MetricView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MetricController @Inject()(
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentNavigatorProvider,
  actions: Actions,
  formProvider: MetricFormProvider,
  service: MetricsService,
  val controllerComponents: MessagesControllerComponents,
  view: MetricView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val prefix: String = "document.metric"

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getMetrics.map {
        metricList =>
          val form = formProvider(prefix, metricList)
          val preparedForm = request.userAnswers.get(MetricPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, lrn, metricList.metrics, mode))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getMetrics.flatMap {
        metricList =>
          val form = formProvider(prefix, metricList)
          form.bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, metricList.metrics, mode))),
            value => {
              implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
              MetricPage.writeToUserAnswers(value).updateTask().writeToSession().navigate()
            }
        )
      }
  }
}
