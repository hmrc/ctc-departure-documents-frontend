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
import forms.DocumentFormProvider
import generators.Generators
import models.{DeclarationType, DocumentList, NormalMode}
import navigation.DocumentsNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import pages.document.PreviousDocumentTypePage
import pages.external.TransitOperationDeclarationTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DocumentsService
import views.html.document.PreviousDocumentTypeView

import scala.concurrent.Future

class PreviousDocumentTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val declarationType          = Gen.oneOf(DeclarationType.T2, DeclarationType.T2F).sample.get
  private val previousDocumentType1    = arbitraryPreviousDocument.arbitrary.sample.get
  private val previousDocumentType2    = arbitraryPreviousDocument.arbitrary.sample.get
  private val previousDocumentTypeList = DocumentList(Seq(previousDocumentType1, previousDocumentType2))

  private val formProvider = new DocumentFormProvider()
  private val form         = formProvider("document.previousDocumentType", previousDocumentTypeList)
  private val mode         = NormalMode

  private val mockDocumentService: DocumentsService = mock[DocumentsService]

  private lazy val previousDocumentTypeRoute = routes.PreviousDocumentTypeController.onPageLoad(lrn, mode, documentIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentsNavigatorProvider]).toInstance(fakeDocumentsNavigatorProvider))
      .overrides(bind(classOf[DocumentsService]).toInstance(mockDocumentService))

  "PreviousDocumentType Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.setValue(TransitOperationDeclarationTypePage, declarationType)

      when(mockDocumentService.getPreviousDocuments()(any())).thenReturn(Future.successful(previousDocumentTypeList))
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, previousDocumentTypeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[PreviousDocumentTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, previousDocumentTypeList.documents, mode, declarationType, documentIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockDocumentService.getPreviousDocuments()(any())).thenReturn(Future.successful(previousDocumentTypeList))
      val userAnswers = emptyUserAnswers
        .setValue(TransitOperationDeclarationTypePage, declarationType)
        .setValue(PreviousDocumentTypePage(documentIndex), previousDocumentType1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, previousDocumentTypeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> previousDocumentType1.code))

      val view = injector.instanceOf[PreviousDocumentTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, previousDocumentTypeList.documents, mode, declarationType, documentIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(TransitOperationDeclarationTypePage, declarationType)

      when(mockDocumentService.getPreviousDocuments()(any())).thenReturn(Future.successful(previousDocumentTypeList))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, previousDocumentTypeRoute)
        .withFormUrlEncodedBody(("value", previousDocumentType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(TransitOperationDeclarationTypePage, declarationType)

      when(mockDocumentService.getPreviousDocuments()(any())).thenReturn(Future.successful(previousDocumentTypeList))
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, previousDocumentTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[PreviousDocumentTypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, previousDocumentTypeList.documents, mode, declarationType, documentIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, previousDocumentTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, previousDocumentTypeRoute)
        .withFormUrlEncodedBody(("value", previousDocumentType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
