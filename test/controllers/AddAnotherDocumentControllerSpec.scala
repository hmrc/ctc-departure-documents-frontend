package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.document.routes
import forms.YesNoFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AddAnotherDocumentPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class AddAnotherDocumentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("addAnotherDocument")
  private val mode         = NormalMode
  private lazy val addAnotherDocumentRoute = routes.AddAnotherDocumentController.onPageLoad(lrn, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentsNavigatorProvider]).toInstance(fakeDocumentsNavigatorProvider))

  "AddAnotherDocument Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, addAnotherDocumentRoute)
      val result = route(app, request).value

      val view = injector.instanceOf[AddAnotherDocumentView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(AddAnotherDocumentPage, true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addAnotherDocumentRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddAnotherDocumentView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, addAnotherDocumentRoute)
      .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, addAnotherDocumentRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AddAnotherDocumentView]

      contentAsString(result) mustEqual
        view(boundForm, lrn, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherDocumentRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherDocumentRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
