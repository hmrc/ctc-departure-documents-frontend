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

package viewModels.document

import base.SpecBase
import generators.Generators
import models.Index
import models.reference.Document
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.{DocumentReferenceNumberPage, PreviousDocumentTypePage, TypePage}
import org.scalacheck.Arbitrary.arbitrary

class DocumentReferenceNumberViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Document reference number view model" - {

    "must return empty list" - {
      "when user answers unpopulated" in {
        val viewModel = DocumentReferenceNumberViewModel.apply(emptyUserAnswers, index)
        viewModel.otherReferenceNumbers mustBe Nil
      }
    }

    "must return list of other reference numbers with same type" - {
      "when there is another document of the given type" - {
        "and reference number undefined at given index" - {
          "when TypePage" in {
            forAll(arbitrary[Document], nonEmptyString) {
              (document, referenceNumber) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TypePage(Index(0)), document)
                  .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)
                  .setValue(TypePage(Index(1)), document)

                val viewModel = DocumentReferenceNumberViewModel.apply(userAnswers, Index(1))
                viewModel.otherReferenceNumbers mustBe Seq(referenceNumber)
            }
          }

          "when PreviousDocumentTypePage" in {
            forAll(arbitrary[Document], nonEmptyString) {
              (document, referenceNumber) =>
                val userAnswers = emptyUserAnswers
                  .setValue(PreviousDocumentTypePage(Index(0)), document)
                  .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)
                  .setValue(PreviousDocumentTypePage(Index(1)), document)

                val viewModel = DocumentReferenceNumberViewModel.apply(userAnswers, Index(1))
                viewModel.otherReferenceNumbers mustBe Seq(referenceNumber)
            }
          }
        }

        "and reference number defined at given index" in {
          forAll(arbitrary[Document], nonEmptyString, nonEmptyString) {
            (document, referenceNumber1, referenceNumber2) =>
              val userAnswers = emptyUserAnswers
                .setValue(TypePage(Index(0)), document)
                .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber1)
                .setValue(TypePage(Index(1)), document)
                .setValue(DocumentReferenceNumberPage(Index(1)), referenceNumber2)

              val viewModel = DocumentReferenceNumberViewModel.apply(userAnswers, Index(1))
              viewModel.otherReferenceNumbers mustBe Seq(referenceNumber1)
          }
        }
      }

      "when there is not another document of the given type" in {
        forAll(arbitrary[Document], nonEmptyString) {
          (document1, referenceNumber) =>
            forAll(arbitrary[Document].retryUntil(_ != document1)) {
              document2 =>
                val userAnswers = emptyUserAnswers
                  .setValue(TypePage(Index(0)), document1)
                  .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)
                  .setValue(TypePage(Index(1)), document2)

                val viewModel = DocumentReferenceNumberViewModel.apply(userAnswers, Index(1))
                viewModel.otherReferenceNumbers mustBe Nil
            }
        }
      }

      "when reference number undefined at given index" in {
        forAll(arbitrary[Document], nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(TypePage(Index(0)), document)
              .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)
              .setValue(TypePage(Index(1)), document)

            val viewModel = DocumentReferenceNumberViewModel.apply(userAnswers, Index(1))
            viewModel.otherReferenceNumbers mustBe Seq(referenceNumber)
        }
      }
    }
  }

}
