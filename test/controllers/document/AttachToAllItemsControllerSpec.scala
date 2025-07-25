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
import forms.YesNoFormProvider
import generators.{ConsignmentLevelDocumentsGenerator, Generators}
import models.{Index, NormalMode, UserAnswers}
import navigation.DocumentNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.document.{AttachToAllItemsPage, InferredAttachToAllItemsPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.document.AttachToAllItemsView

import scala.concurrent.Future

class AttachToAllItemsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ConsignmentLevelDocumentsGenerator {

  private val formProvider               = new YesNoFormProvider()
  private val form                       = formProvider("document.attachToAllItems")
  private val mode                       = NormalMode
  private lazy val attachToAllItemsRoute = routes.AttachToAllItemsController.onPageLoad(lrn, mode, documentIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentNavigatorProvider]).toInstance(fakeDocumentNavigatorProvider))

  "AttachToAllItems Controller" - {

    "In PostTransition" - {

      "must set inferred value to false if cannot add any more consignment level documents" in {

        val documentIndex = Index(numberOfDocuments)
        setExistingUserAnswers(userAnswersWithConsignmentLevelDocumentsMaxedOut)

        lazy val attachToAllItemsRoute = routes.AttachToAllItemsController.onPageLoad(lrn, mode, documentIndex).url

        val request = FakeRequest(GET, attachToAllItemsRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
        userAnswersCaptor.getValue.get(InferredAttachToAllItemsPage(documentIndex)).value mustEqual false
      }

      "must return OK and the correct view for a GET" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, attachToAllItemsRoute)
        val result  = route(app, request).value

        val view = injector.instanceOf[AttachToAllItemsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, lrn, mode, documentIndex)(request, messages).toString
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers.setValue(AttachToAllItemsPage(documentIndex), true)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, attachToAllItemsRoute)

        val result = route(app, request).value

        val filledForm = form.bind(Map("value" -> "true"))

        val view = injector.instanceOf[AttachToAllItemsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, mode, documentIndex)(request, messages).toString
      }

      "must redirect to the next page when valid data is submitted" in {

        when(mockSessionRepository.set(any())(any())) `thenReturn` Future.successful(true)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, attachToAllItemsRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request   = FakeRequest(POST, attachToAllItemsRoute).withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val view = injector.instanceOf[AttachToAllItemsView]

        contentAsString(result) mustEqual
          view(boundForm, lrn, mode, documentIndex)(request, messages).toString
      }

      "must redirect to Session Expired for a GET if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, attachToAllItemsRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "must redirect to Session Expired for a POST if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, attachToAllItemsRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }
    }
  }
}
