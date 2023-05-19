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

package models

import base.SpecBase
import generators.Generators
import models.reference.Document
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.{AttachToAllItemsPage, PreviousDocumentTypePage, TypePage}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

class ConsignmentLevelDocumentsSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Consignment Level Documents" - {

    "must return counts of each document type at consignment level" - {

      "when there is a previous document" - {

        val pageGen = Gen.oneOf(TypePage, PreviousDocumentTypePage)

        "at item level" in {
          forAll(pageGen, arbitrary[Document](arbitraryPreviousDocument)) {
            (typePage, document) =>
              val userAnswers = emptyUserAnswers
                .setValue(AttachToAllItemsPage(index), false)
                .setValue(typePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers)
              result.previous mustBe 0
              result.supporting mustBe 0
              result.transport mustBe 0
          }
        }

        "at consignment level" in {
          forAll(pageGen, arbitrary[Document](arbitraryPreviousDocument)) {
            (typePage, document) =>
              val userAnswers = emptyUserAnswers
                .setValue(AttachToAllItemsPage(index), true)
                .setValue(typePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers)
              result.previous mustBe 1
              result.supporting mustBe 0
              result.transport mustBe 0
          }
        }
      }

      "when there is a supporting document" - {

        "at item level" in {
          forAll(arbitrary[Document](arbitrarySupportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(AttachToAllItemsPage(index), false)
                .setValue(TypePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers)
              result.previous mustBe 0
              result.supporting mustBe 0
              result.transport mustBe 0
          }
        }

        "at consignment level" in {
          forAll(arbitrary[Document](arbitrarySupportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(AttachToAllItemsPage(index), true)
                .setValue(TypePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers)
              result.previous mustBe 0
              result.supporting mustBe 1
              result.transport mustBe 0
          }
        }
      }

      "when there is a transport document" - {

        "at item level" in {
          forAll(arbitrary[Document](arbitraryTransportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(AttachToAllItemsPage(index), false)
                .setValue(TypePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers)
              result.previous mustBe 0
              result.supporting mustBe 0
              result.transport mustBe 0
          }
        }

        "at consignment level" in {
          forAll(arbitrary[Document](arbitraryTransportDocument)) {
            document =>
              val userAnswers = emptyUserAnswers
                .setValue(AttachToAllItemsPage(index), true)
                .setValue(TypePage(index), document)

              val result = ConsignmentLevelDocuments.apply(userAnswers)
              result.previous mustBe 0
              result.supporting mustBe 0
              result.transport mustBe 1
          }
        }
      }
    }
  }

}
