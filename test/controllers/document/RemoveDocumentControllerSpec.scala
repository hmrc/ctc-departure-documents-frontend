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
import forms.YesNoFormProvider
import generators.Generators
import models.reference.Document
import models.{Index, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.document.{DocumentReferenceNumberPage, DocumentUuidPage, PreviousDocumentTypePage, TypePage}
import pages.external.DocumentPage
import pages.sections.DocumentSection
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.document.RemoveDocumentView

import java.util.UUID
import scala.concurrent.Future

class RemoveDocumentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new YesNoFormProvider()

  private def form(documentType: Document): Form[Boolean] =
    formProvider("document.removeDocument", documentType)

  private lazy val removeDocumentRoute = routes.RemoveDocumentController.onPageLoad(lrn, documentIndex).url
  private val document                 = arbitrary[Document].sample.value
  private val documentReferenceNumber  = Gen.alphaNumStr.sample.value
  private val insetText                = s"${document.asString} - $documentReferenceNumber"
  private val typePage                 = Gen.oneOf(TypePage, PreviousDocumentTypePage).sample.value
  private val uuid                     = arbitrary[UUID].sample.value

  "RemoveDocument Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .setValue(typePage(documentIndex), document)
        .setValue(DocumentReferenceNumberPage(documentIndex), documentReferenceNumber)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, removeDocumentRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[RemoveDocumentView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form(document), lrn, documentIndex, insetText)(request, messages).toString
    }

    "when yes submitted" - {
      "must redirect to add another document and remove document at specified index" in {
        reset(mockSessionRepository)
        when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .setValue(typePage(documentIndex), document)
          .setValue(DocumentUuidPage(documentIndex), uuid)
          .setValue(DocumentPage(Index(0), Index(0)), uuid)
          .setValue(DocumentPage(Index(1), Index(0)), uuid)
          .setValue(DocumentReferenceNumberPage(documentIndex), documentReferenceNumber)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeDocumentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          s"http://localhost:10127/manage-transit-movements/departures/items/$lrn/update-task?" +
          s"continue=http://localhost:10132${controllers.routes.AddAnotherDocumentController.onPageLoad(lrn).url}"

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())

        userAnswersCaptor.getValue.get(DocumentSection(documentIndex)) mustNot be(defined)
        userAnswersCaptor.getValue.get(DocumentPage(Index(0), Index(0))) mustNot be(defined)
        userAnswersCaptor.getValue.get(DocumentPage(Index(0), Index(1))) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to add another document and not remove document at specified index" in {
        reset(mockSessionRepository)
        when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

        val userAnswers = emptyUserAnswers
          .setValue(typePage(documentIndex), document)
          .setValue(DocumentReferenceNumberPage(documentIndex), documentReferenceNumber)

        setExistingUserAnswers(userAnswers)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeDocumentRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddAnotherDocumentController.onPageLoad(lrn).url

        verify(mockSessionRepository, never()).set(any())(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers
        .setValue(typePage(documentIndex), document)
        .setValue(DocumentReferenceNumberPage(documentIndex), documentReferenceNumber)

      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, removeDocumentRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form(document).bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RemoveDocumentView]

      contentAsString(result) mustEqual
        view(boundForm, lrn, documentIndex, insetText)(request, messages).toString
    }

    "must redirect to Session Expired for a GET" - {
      "if no existing data found" in {
        setNoExistingUserAnswers()

        val request = FakeRequest(GET, removeDocumentRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
      }

      "if no document is found" in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, removeDocumentRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.AddAnotherDocumentController.onPageLoad(lrn).url
      }
    }

    "must redirect to Session Expired for a POST" - {
      "if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, removeDocumentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
      }

      "if no document is found" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeDocumentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.AddAnotherDocumentController.onPageLoad(lrn).url
      }
    }
  }
}
