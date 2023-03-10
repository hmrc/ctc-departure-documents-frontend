package controllers.document

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.DocumentTypeFormProvider
import models.{Mode, LocalReferenceNumber}
import navigation.UserAnswersNavigator
import pages.document.TypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DocumentTypesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.document.TypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeController @Inject()(
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: DocumentsNavigatorNavigatorProvider,
  actions: Actions,
  formProvider: DocumentTypeFormProvider,
  service: DocumentTypesService,
  val controllerComponents: MessagesControllerComponents,
  view: TypeView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val prefix: String = "document.type"

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getDocumentTypes.map {
        documentTypeList =>
          val form = formProvider(prefix, documentTypeList)
          val preparedForm = request.userAnswers.get(TypePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, lrn, documentTypeList.documentTypes, mode))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      service.getDocumentTypes.flatMap {
        documentTypeList =>
          val form = formProvider(prefix, documentTypeList)
          form.bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, documentTypeList.documentTypes, mode))),
            value => {
              implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
              TypePage.writeToUserAnswers(value).updateTask().writeToSession().navigate()
            }
        )
      }
  }
}
