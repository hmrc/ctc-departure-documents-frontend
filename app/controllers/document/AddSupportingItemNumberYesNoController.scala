package controllers.document

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.{Mode, LocalReferenceNumber}
import navigation.UserAnswersNavigator
import pages.document.AddSupportingItemNumberYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.AddSupportingItemNumberYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddSupportingItemNumberYesNoController @Inject()(
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentsNavigatorProvider,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddSupportingItemNumberYesNoView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider("document.addSupportingItemNumberYesNo")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AddSupportingItemNumberYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, lrn, mode))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode))),
        value => {
          implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
          AddSupportingItemNumberYesNoPage.writeToUserAnswers(value).updateTask().writeToSession().navigate()
        }
      )
  }
}
