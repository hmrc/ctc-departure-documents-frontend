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

package navigation

import base.SpecBase
import config.FrontendAppConfig
import generators.Generators
import models.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DocumentNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  "Document Navigator" - {

    "when in NormalMode" - {

      val mode              = NormalMode
      val navigatorProvider = new DocumentNavigatorProviderImpl()(mockFrontendAppConfig)
      val navigator         = navigatorProvider.apply(mode, index)

      "when answers complete" - {
        "must redirect to check your answers for document" in {
          forAll(arbitraryDocumentAnswers(emptyUserAnswers, index)) {
            answers =>
              navigator
                .nextPage(answers, None)
                .mustEqual(controllers.document.routes.DocumentAnswersController.onPageLoad(lrn, index))
          }
        }
      }
    }

    "when in CheckMode" - {

      val mode              = CheckMode
      val navigatorProvider = new DocumentNavigatorProviderImpl()(mockFrontendAppConfig)
      val navigator         = navigatorProvider.apply(mode, index)

      "when answers complete" - {
        "must redirect to check your answers for document" in {
          forAll(arbitraryDocumentAnswers(emptyUserAnswers, index)) {
            answers =>
              navigator
                .nextPage(answers, None)
                .mustEqual(controllers.document.routes.DocumentAnswersController.onPageLoad(lrn, index))
          }
        }
      }
    }
  }
}
