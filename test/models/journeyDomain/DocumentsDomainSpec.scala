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

package models.journeyDomain

import base.SpecBase
import generators.Generators
import models.Index
import org.scalacheck.Gen

class DocumentsDomainSpec extends SpecBase with Generators {

  "Documents" - {

    "can be parsed from UserAnswers" in {

      val numberOfItems = Gen.choose(1, frontendAppConfig.maxDocuments).sample.value

      val userAnswers = (0 until numberOfItems).foldLeft(emptyUserAnswers)({
        case (updatedUserAnswers, index) =>
          arbitraryDocumentAnswers(updatedUserAnswers, Index(index)).sample.value
      })

      val result: EitherType[DocumentsDomain] = UserAnswersReader[DocumentsDomain].run(userAnswers)

      result.value.document.length mustBe numberOfItems

    }
  }
}
