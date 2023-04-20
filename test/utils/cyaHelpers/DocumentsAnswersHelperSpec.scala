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

package utils.cyaHelpers

import base.SpecBase
import controllers.document.routes
import generators.Generators
import models.DeclarationType._
import models.reference.{CustomsOffice, Document}
import models.{DeclarationType, Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}
import viewModels.ListItem

class DocumentsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "DocumentsAnswersHelper" - {

    "listItems" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val helper = new DocumentsAnswersHelper(userAnswers, mode)
              helper.listItems mustBe Nil
          }
        }
      }

      "when user answers populated with complete documents" - {
        "when Type page is populated" - {
          "must return list items with remove links" in {
            forAll(
              arbitrary[Mode],
              arbitrary[CustomsOffice](arbitraryXiCustomsOffice),
              arbitrary[DeclarationType],
              arbitrary[Document](arbitraryTransportDocument),
              nonEmptyString
            ) {
              (mode, xiCustomsOffice, declarationType, document, referenceNumber) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationOfficeOfDeparturePage, xiCustomsOffice)
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TypePage(Index(0)), document)
                  .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)

                val helper = new DocumentsAnswersHelper(userAnswers, mode)

                helper.listItems mustBe Seq(
                  Right(
                    ListItem(
                      name = s"${document.toString} - reference number $referenceNumber",
                      changeUrl = routes.DocumentAnswersController.onPageLoad(userAnswers.lrn, mode, documentIndex).url,
                      removeUrl = Some(routes.RemoveDocumentController.onPageLoad(lrn, mode, documentIndex).url)
                    )
                  )
                )
            }
          }

          "when Previous Type page is populated" - {
            "must return list items with remove links" in {
              forAll(
                arbitrary[Mode],
                arbitrary[CustomsOffice](arbitraryGbCustomsOffice),
                Gen.oneOf(T2, T2F),
                arbitrary[Document](arbitraryPreviousDocument),
                nonEmptyString
              ) {
                (mode, gbCustomsOffice, declarationType, previousDocument, referenceNumber) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationOfficeOfDeparturePage, gbCustomsOffice)
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(PreviousDocumentTypePage(Index(0)), previousDocument)
                    .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)
                    .setValue(AddGoodsItemNumberYesNoPage(Index(0)), false)
                    .setValue(AddTypeOfPackageYesNoPage(Index(0)), false)
                    .setValue(DeclareQuantityOfGoodsYesNoPage(Index(0)), false)

                  val helper = new DocumentsAnswersHelper(userAnswers, mode)

                  helper.listItems mustBe Seq(
                    Right(
                      ListItem(
                        name = s"${previousDocument.toString} - reference number $referenceNumber",
                        changeUrl = routes.DocumentAnswersController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                        removeUrl = None
                      )
                    )
                  )
              }
            }
          }
        }

        "when user answers populated with an in progress document" - {
          "when Type page is populated" - {
            "must return list items with remove links" in {
              forAll(
                arbitrary[Mode],
                arbitrary[CustomsOffice](arbitraryXiCustomsOffice),
                arbitrary[DeclarationType],
                arbitrary[Document]
              ) {
                (mode, xiCustomsOffice, declarationType, document) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationOfficeOfDeparturePage, xiCustomsOffice)
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(TypePage(Index(0)), document)

                  val helper = new DocumentsAnswersHelper(userAnswers, mode)

                  helper.listItems mustBe Seq(
                    Left(
                      ListItem(
                        name = document.toString,
                        changeUrl = controllers.document.routes.DocumentReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                        removeUrl = Some(routes.RemoveDocumentController.onPageLoad(lrn, mode, Index(0)).url)
                      )
                    )
                  )
              }
            }
          }

          "when Previous Type page is populated" - {
            "must return list items with remove links" in {
              forAll(
                arbitrary[Mode],
                arbitrary[Document](arbitraryPreviousDocument),
                Gen.oneOf(T2, T2F),
                arbitraryGbCustomsOffice
              ) {
                (mode, previousDocument, declarationType, gbCustomsOffice) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(TransitOperationOfficeOfDeparturePage, gbCustomsOffice.arbitrary.sample.get)
                    .setValue(PreviousDocumentTypePage(Index(0)), previousDocument)

                  val helper = new DocumentsAnswersHelper(userAnswers, mode)

                  helper.listItems mustBe Seq(
                    Left(
                      ListItem(
                        name = previousDocument.toString,
                        changeUrl = controllers.document.routes.DocumentReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                        removeUrl = None
                      )
                    )
                  )
              }
            }
          }
        }
      }
    }
  }

}
