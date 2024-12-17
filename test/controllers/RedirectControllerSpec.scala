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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.Document
import models.{Index, NormalMode, UserAnswers}
import navigation.DocumentsNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.{InferredAttachToAllItemsPage, TypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class RedirectControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[DocumentsNavigatorProvider]).toInstance(fakeDocumentsNavigatorProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  "Redirect Controller" - {

    "redirect" - {

      lazy val redirectRoute = routes.RedirectController.redirect(lrn).url

      "must redirect to the 'add another' page" in {
        val userAnswers = emptyUserAnswers
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, redirectRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "mandatoryPrevious" - {

      lazy val mandatoryPreviousRoute = routes.RedirectController.mandatoryPrevious(lrn).url

      "must infer next index as consignment level and redirect" in {
        forAll(arbitrary[Document]) {
          document =>
            beforeEach()

            when(mockSessionRepository.set(any())(any()))
              .thenReturn(Future.successful(true))

            val userAnswers = emptyUserAnswers
              .setValue(TypePage(Index(0)), document)

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(GET, mandatoryPreviousRoute)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            val nextIndex = Index(1)

            redirectLocation(result).value mustEqual
              controllers.document.routes.PreviousDocumentTypeController.onPageLoad(lrn, NormalMode, nextIndex).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
            userAnswersCaptor.getValue.get(InferredAttachToAllItemsPage(nextIndex)).value mustEqual false
        }
      }
    }
  }
}
