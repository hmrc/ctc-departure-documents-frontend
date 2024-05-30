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
import forms.DocumentReferenceNumberFormProvider
import generators.Generators
import models.{NormalMode, UserAnswers}
import navigation.DocumentNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages.document.{DocumentReferenceNumberPage, DocumentUuidPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.document.DocumentReferenceNumberViewModel
import viewModels.document.DocumentReferenceNumberViewModel.DocumentReferenceNumberViewModelProvider
import views.html.document.DocumentReferenceNumberView

import java.util.UUID
import scala.concurrent.Future

class DocumentReferenceNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val otherReferenceNumbers = listWithMaxLength[String]()(Arbitrary(nonEmptyString)).sample.value

  private lazy val formProvider                 = new DocumentReferenceNumberFormProvider()
  private lazy val form                         = formProvider("document.documentReferenceNumber", otherReferenceNumbers)
  private val mode                              = NormalMode
  private val validAnswer                       = "testString123"
  private lazy val documentReferenceNumberRoute = routes.DocumentReferenceNumberController.onPageLoad(lrn, mode, documentIndex).url

  private val mockViewModelProvider: DocumentReferenceNumberViewModelProvider = mock[DocumentReferenceNumberViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentNavigatorProvider]).toInstance(fakeDocumentNavigatorProvider))
      .overrides(bind(classOf[DocumentReferenceNumberViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
    when(mockViewModelProvider.apply(any(), any())).thenReturn(DocumentReferenceNumberViewModel(otherReferenceNumbers))
  }

  "DocumentReferenceNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, documentReferenceNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[DocumentReferenceNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode, documentIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(DocumentReferenceNumberPage(documentIndex), validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, documentReferenceNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> validAnswer))

      val view = injector.instanceOf[DocumentReferenceNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, documentIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" - {
      "and UUID not populated" in {
        setExistingUserAnswers(emptyUserAnswers)

        when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

        val request = FakeRequest(POST, documentReferenceNumberRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())

        userAnswersCaptor.getValue.get(DocumentUuidPage(documentIndex)) must be(defined)
      }

      "and UUID populated" in {
        val uuid = arbitrary[UUID].sample.value

        val userAnswers = emptyUserAnswers.setValue(DocumentUuidPage(documentIndex), uuid)
        setExistingUserAnswers(userAnswers)

        when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

        val request = FakeRequest(POST, documentReferenceNumberRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())

        userAnswersCaptor.getValue.get(DocumentUuidPage(documentIndex)).value mustBe uuid
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, documentReferenceNumberRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[DocumentReferenceNumberView]

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, documentIndex)(request, messages).toString
    }

    "must return a Bad Request and errors when non-unique reference number is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = otherReferenceNumbers.head

      val request    = FakeRequest(POST, documentReferenceNumberRoute).withFormUrlEncodedBody(("value", invalidAnswer))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[DocumentReferenceNumberView]

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, documentIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, documentReferenceNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, documentReferenceNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
