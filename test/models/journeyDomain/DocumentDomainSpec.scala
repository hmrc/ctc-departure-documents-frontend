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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.PreviousDocumentTypePage

class DocumentDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Document Domain" - {

    "can be read from user answers" - {
      "when previous document type page is answered" in {
        val previousDocType = arbitraryPreviousDocumentType.arbitrary.sample.get

        val userAnswers = emptyUserAnswers
          .setValue(PreviousDocumentTypePage(documentIndex), previousDocType)

        val expectedResult = DocumentDomain(previousDocType)

        val result: EitherType[DocumentDomain] =
          UserAnswersReader[DocumentDomain](DocumentDomain.userAnswersReader(documentIndex)).run(userAnswers)

        result.value mustBe expectedResult
      }
    }

    "can not be read from user answers" - {
      "when item description page is unanswered" in {
        val result: EitherType[DocumentDomain] =
          UserAnswersReader[DocumentDomain](DocumentDomain.userAnswersReader(documentIndex)).run(emptyUserAnswers)

        result.left.value.page mustBe PreviousDocumentTypePage(documentIndex)
      }
    }
  }

}
