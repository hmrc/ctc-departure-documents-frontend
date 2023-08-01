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
import config.{PhaseConfig, TransitionConfig}
import forms.YesNoFormProvider
import generators.{ConsignmentLevelDocumentsGenerator, Generators}
import models.{Index, NormalMode, UserAnswers}
import navigation.DocumentNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import pages.document.InferredAttachToAllItemsPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AttachToAllItemsTransitionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ConsignmentLevelDocumentsGenerator {

  private val formProvider               = new YesNoFormProvider()
  private val form                       = formProvider("document.attachToAllItems")
  private val mode                       = NormalMode
  private lazy val attachToAllItemsRoute = routes.AttachToAllItemsController.onPageLoad(lrn, mode, documentIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[PhaseConfig]).to(classOf[TransitionConfig]))
      .overrides(bind(classOf[DocumentNavigatorProvider]).toInstance(fakeDocumentNavigatorProvider))

  "AttachToAllItems Controller" - {

    "In Transition" - {

      "must set inferred value to false and redirect" in {

        val documentIndex = Index(numberOfDocuments)
        setExistingUserAnswers(userAnswersWithConsignmentLevelDocumentsMaxedOut)

        lazy val attachToAllItemsRoute = routes.AttachToAllItemsController.onPageLoad(lrn, mode, documentIndex).url

        val request = FakeRequest(GET, attachToAllItemsRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
        userAnswersCaptor.getValue.get(InferredAttachToAllItemsPage(documentIndex)).value mustBe false
      }
    }
  }
}
