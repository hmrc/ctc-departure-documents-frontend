/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.MetricFormProvider
import models.{MetricList, NormalMode}
import generators.Generators
import navigation.DocumentNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.document.MetricPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.MetricsService
import views.html.document.MetricView

import scala.concurrent.Future

class MetricControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val metric1    = arbitraryMetric.arbitrary.sample.get
  private val metric2    = arbitraryMetric.arbitrary.sample.get
  private val metricList = MetricList(Seq(metric1, metric2))

  private val formProvider = new MetricFormProvider()
  private val form         = formProvider("document.metric", metricList)
  private val mode         = NormalMode

  private val mockMetricsService: MetricsService = mock[MetricsService]
  private lazy val metricRoute                   = routes.MetricController.onPageLoad(lrn, mode, documentIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentNavigatorProvider]).toInstance(fakeDocumentNavigatorProvider))
      .overrides(bind(classOf[MetricsService]).toInstance(mockMetricsService))

  "Metric Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockMetricsService.getMetrics()(any())).thenReturn(Future.successful(metricList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, metricRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[MetricView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, metricList.metrics, mode, documentIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockMetricsService.getMetrics()(any())).thenReturn(Future.successful(metricList))
      val userAnswers = emptyUserAnswers.setValue(MetricPage(documentIndex), metric1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, metricRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> metric1.code))

      val view = injector.instanceOf[MetricView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, metricList.metrics, mode, documentIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockMetricsService.getMetrics()(any())).thenReturn(Future.successful(metricList))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, metricRoute)
        .withFormUrlEncodedBody(("value", metric1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockMetricsService.getMetrics()(any())).thenReturn(Future.successful(metricList))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, metricRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[MetricView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, metricList.metrics, mode, documentIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, metricRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, metricRoute)
        .withFormUrlEncodedBody(("value", metric1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
