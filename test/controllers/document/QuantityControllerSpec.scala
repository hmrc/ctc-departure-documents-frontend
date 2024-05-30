/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.document

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.BigDecimalFormProvider
import generators.Generators
import models.NormalMode
import models.reference.Metric
import navigation.DocumentNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.document.{MetricPage, QuantityPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.document.QuantityView

import scala.concurrent.Future

class QuantityControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider       = new BigDecimalFormProvider()
  private val form               = formProvider("document.quantity")
  private val mode               = NormalMode
  private val validAnswer        = BigDecimal(1)
  private lazy val quantityRoute = routes.QuantityController.onPageLoad(lrn, mode, index).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentNavigatorProvider]).toInstance(fakeDocumentNavigatorProvider))

  private val metric = arbitrary[Metric].sample.value

  private val baseAnswers = emptyUserAnswers.setValue(MetricPage(index), metric)

  "Quantity Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(GET, quantityRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[QuantityView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode, index, metric)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.setValue(QuantityPage(index), validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, quantityRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> validAnswer.toString))

      val view = injector.instanceOf[QuantityView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, index, metric)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(baseAnswers)

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, quantityRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(baseAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, quantityRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[QuantityView]

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, index, metric)(request, messages).toString
    }

    "must redirect to Session Expired for a GET" - {
      "when no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, quantityRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
      }

      "when metric is undefined" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, quantityRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
      }
    }

    "must redirect to Session Expired for a POST" - {

      "when no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, quantityRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
      }

      "when metric is undefined" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, quantityRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
      }
    }
  }
}
