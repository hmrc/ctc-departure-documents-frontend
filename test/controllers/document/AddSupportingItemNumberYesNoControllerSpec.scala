package controllers.document

import base.{SpecBase, AppWithDefaultMockFixtures}
import forms.YesNoFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.document.AddSupportingItemNumberYesNoPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.document.AddSupportingItemNumberYesNoView

import scala.concurrent.Future

class AddSupportingItemNumberYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("document.addSupportingItemNumberYesNo")
  private val mode         = NormalMode
  private lazy val addSupportingItemNumberYesNoRoute = routes.AddSupportingItemNumberYesNoController.onPageLoad(lrn, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentsNavigatorProvider]).toInstance(fakeDocumentsNavigatorProvider))

  "AddSupportingItemNumberYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, addSupportingItemNumberYesNoRoute)
      val result = route(app, request).value

      val view = injector.instanceOf[AddSupportingItemNumberYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(AddSupportingItemNumberYesNoPage, true)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addSupportingItemNumberYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddSupportingItemNumberYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, addSupportingItemNumberYesNoRoute)
      .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, addSupportingItemNumberYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AddSupportingItemNumberYesNoView]

      contentAsString(result) mustEqual
        view(boundForm, lrn, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addSupportingItemNumberYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addSupportingItemNumberYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
