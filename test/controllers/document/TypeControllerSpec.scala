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
import forms.DocumentFormProvider
import generators.Generators
import models.{ConsignmentLevelDocuments, NormalMode, SelectableList}
import navigation.DocumentNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.document.{AttachToAllItemsPage, InferredAttachToAllItemsPage, TypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DocumentsService
import views.html.document.TypeView

import scala.concurrent.Future

class TypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val document1    = arbitraryDocument.arbitrary.sample.get
  private val document2    = arbitraryDocument.arbitrary.sample.get
  private val documentList = SelectableList(Seq(document1, document2))

  private val attachedToAllItems = arbitrary[Boolean].sample.value

  private val baseAnswers = {
    val page = Gen.oneOf(AttachToAllItemsPage(documentIndex), InferredAttachToAllItemsPage(documentIndex)).sample.value
    emptyUserAnswers.setValue(page, attachedToAllItems)
  }

  private lazy val formProvider = new DocumentFormProvider()
  private lazy val form         = formProvider("document.type", documentList, ConsignmentLevelDocuments(), attachedToAllItems)
  private val mode              = NormalMode

  private val mockDocumentTypesService: DocumentsService = mock[DocumentsService]
  private lazy val typeRoute                             = routes.TypeController.onPageLoad(lrn, mode, documentIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentNavigatorProvider]).toInstance(fakeDocumentNavigatorProvider))
      .overrides(bind(classOf[DocumentsService]).toInstance(mockDocumentTypesService))

  "Type Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockDocumentTypesService.getDocuments(any())(any())).thenReturn(Future.successful(documentList))
      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(GET, typeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[TypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, documentList.values, mode, documentIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockDocumentTypesService.getDocuments(any())(any())).thenReturn(Future.successful(documentList))
      val userAnswers = baseAnswers.setValue(TypePage(documentIndex), document1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, typeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("document" -> document1.toString))

      val view = injector.instanceOf[TypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, documentList.values, mode, documentIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockDocumentTypesService.getDocuments(any())(any())).thenReturn(Future.successful(documentList))
      when(mockSessionRepository.set(any())(any())) `thenReturn` Future.successful(true)

      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(POST, typeRoute)
        .withFormUrlEncodedBody(("document", document1.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockDocumentTypesService.getDocuments(any())(any())).thenReturn(Future.successful(documentList))
      setExistingUserAnswers(baseAnswers)

      val request   = FakeRequest(POST, typeRoute).withFormUrlEncodedBody(("document", "invalid value"))
      val boundForm = form.bind(Map("document" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[TypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, documentList.values, mode, documentIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, typeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, typeRoute)
        .withFormUrlEncodedBody(("document", document1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
