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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.AddAnotherFormProvider
import generators.Generators
import models.DeclarationType._
import models.{Index, NormalMode}
import navigation.DocumentsNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.AddAnotherDocumentViewModel.AddAnotherDocumentViewModelProvider
import viewModels.{AddAnotherDocumentViewModel, ListItem}
import views.html.AddAnotherDocumentView

class AddAnotherDocumentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherDocumentViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore(frontendAppConfig))

  private lazy val addAnotherDocumentRoute = routes.AddAnotherDocumentController.onPageLoad(lrn).url

  private val mockViewModelProvider = mock[AddAnotherDocumentViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentsNavigatorProvider]).toInstance(fakeDocumentsNavigatorProvider))
      .overrides(bind(classOf[AddAnotherDocumentViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxDocuments - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxDocuments)(listItem)

  private val viewModel = arbitrary[AddAnotherDocumentViewModel].sample.value

  private val emptyViewModel       = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherDocument Controller" - {
    "redirect to type controller when 0 documents added" - {
      "and declaration type is not GB and Office of Departure is not T2 or T2F" in {

        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(emptyViewModel)

        val declarationType     = Gen.oneOf(T1, TIR, T).sample.value
        val xiOfficeOfDeparture = arbitraryXiCustomsOffice.arbitrary.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(TransitOperationDeclarationTypePage, declarationType)
          .setValue(TransitOperationOfficeOfDeparturePage, xiOfficeOfDeparture)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, addAnotherDocumentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.document.routes.TypeController.onPageLoad(lrn, NormalMode, Index(0)).url
      }

      "and declaration type is GB and Office of Departure is T2 or T2F" in {

        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(emptyViewModel)

        val declarationType     = Gen.oneOf(T2, T2F).sample.value
        val gbOfficeOfDeparture = arbitraryGbCustomsOffice.arbitrary.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(TransitOperationDeclarationTypePage, declarationType)
          .setValue(TransitOperationOfficeOfDeparturePage, gbOfficeOfDeparture)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, addAnotherDocumentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.document.routes.PreviousDocumentTypeController.onPageLoad(lrn, NormalMode, Index(0)).url
      }
    }

    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {

        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherDocumentRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherDocumentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {

        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherDocumentRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherDocumentView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), lrn, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to type page at next index" in {
          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherDocumentRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.document.routes.TypeController.onPageLoad(lrn, NormalMode, Index(listItems.length)).url
        }
      }

      "when no submitted" - {
        "must redirect to task list" in {
          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherDocumentRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual frontendAppConfig.taskListUrl(lrn)
        }
      }
    }

    "when max limit reached" - {
      "must redirect to task list" in {
        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherDocumentRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.taskListUrl(lrn)
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherDocumentRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherDocumentView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
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
