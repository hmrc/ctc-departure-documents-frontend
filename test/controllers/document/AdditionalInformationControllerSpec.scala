package controllers.document

import base.{AppWithDefaultMockFixtures, SpecBase}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.document.AdditionalInformationView

class AdditionalInformationControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private lazy val additionalInformationRoute = routes.AdditionalInformationController.onPageLoad(lrn).url

  "AdditionalInformation Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, additionalInformationRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[AdditionalInformationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(lrn)(request, messages).toString
    }
  }
}
