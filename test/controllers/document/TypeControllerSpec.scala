package controllers.document

import base.{SpecBase, AppWithDefaultMockFixtures}
import forms.DocumentTypeFormProvider
import models.{DocumentTypeList, NormalMode}
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.document.TypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DocumentTypesService
import views.html.document.TypeView

import scala.concurrent.Future

class TypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val documentType1 = arbitraryDocumentType.arbitrary.sample.get
  private val documentType2 = arbitraryDocumentType.arbitrary.sample.get
  private val documentTypeList = DocumentTypeList(Seq(documentType1, documentType2))

  private val formProvider = new DocumentTypeFormProvider()
  private val form         = formProvider("document.type", documentTypeList)
  private val mode         = NormalMode

  private val mockDocumentTypesService: DocumentTypesService = mock[DocumentTypesService]
  private lazy val typeRoute = routes.TypeController.onPageLoad(lrn, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentsNavigatorNavigatorProvider]).toInstance(fakeDocumentsNavigatorNavigatorProvider))
      .overrides(bind(classOf[DocumentTypesService]).toInstance(mockDocumentTypesService))

  "Type Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockDocumentTypesService.getDocumentTypes(any())).thenReturn(Future.successful(documentTypeList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, typeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[TypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, documentTypeList.documentTypes, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockDocumentTypesService.getDocumentTypes(any())).thenReturn(Future.successful(documentTypeList))
      val userAnswers = emptyUserAnswers.setValue(TypePage, documentType1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, typeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> documentType1.id))

      val view = injector.instanceOf[TypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, documentTypeList.documentTypes, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockDocumentTypesService.getDocumentTypes(any())).thenReturn(Future.successful(documentTypeList))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, typeRoute)
        .withFormUrlEncodedBody(("value", documentType1.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockDocumentTypesService.getDocumentTypes(any())).thenReturn(Future.successful(documentTypeList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, typeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[TypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, documentTypeList.documentTypes, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, typeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, typeRoute)
        .withFormUrlEncodedBody(("value", documentType1.id))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
