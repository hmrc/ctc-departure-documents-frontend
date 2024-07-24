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
import models.reference.{CustomsOffice, Document}
import models.{Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}
import viewModels.ListItem

class DocumentsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "DocumentsAnswersHelper" - {

    "listItems" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          val userAnswers = emptyUserAnswers

          val helper = new DocumentsAnswersHelper(userAnswers)
          helper.listItems mustBe Nil
        }
      }

      "when user answers populated with complete documents" - {
        "when Type page is populated" - {
          "must return list items with remove links" in {
            forAll(
              arbitrary[CustomsOffice](arbitraryXiCustomsOffice),
              arbitrary[String](arbitraryDeclarationType),
              arbitrary[Boolean],
              arbitrary[Document](arbitraryTransportDocument),
              nonEmptyString
            ) {
              (xiCustomsOffice, declarationType, attachToAllItems, document, referenceNumber) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationOfficeOfDeparturePage, xiCustomsOffice)
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(AttachToAllItemsPage(Index(0)), attachToAllItems)
                  .setValue(TypePage(Index(0)), document)
                  .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)

                val helper = new DocumentsAnswersHelper(userAnswers)

                helper.listItems mustBe Seq(
                  Right(
                    ListItem(
                      name = s"$document - $referenceNumber",
                      changeUrl = routes.DocumentAnswersController.onPageLoad(userAnswers.lrn, documentIndex).url,
                      removeUrl = Some(routes.RemoveDocumentController.onPageLoad(lrn, documentIndex).url)
                    )
                  )
                )
            }
          }

          "when Previous Type page is populated" - {
            "must return list items with remove links" in {
              forAll(
                arbitrary[CustomsOffice](arbitraryGbCustomsOffice),
                arbitrary[String](arbitraryT2OrT2FDeclarationType),
                arbitrary[Boolean],
                arbitrary[Document](arbitraryPreviousDocument),
                nonEmptyString
              ) {
                (gbCustomsOffice, declarationType, attachToAllItems, previousDocument, referenceNumber) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationOfficeOfDeparturePage, gbCustomsOffice)
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(AttachToAllItemsPage(Index(0)), attachToAllItems)
                    .setValue(PreviousDocumentTypePage(Index(0)), previousDocument)
                    .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)
                    .setValue(AddAdditionalInformationYesNoPage(Index(0)), false)

                  val helper = new DocumentsAnswersHelper(userAnswers)

                  helper.listItems mustBe Seq(
                    Right(
                      ListItem(
                        name = s"$previousDocument - $referenceNumber",
                        changeUrl = routes.DocumentAnswersController.onPageLoad(userAnswers.lrn, Index(0)).url,
                        removeUrl = None
                      )
                    )
                  )
              }
            }
          }
        }

        "when user answers populated with an in progress document" - {
          "when Type page is unpopulated" - {
            "must return list items with remove links" in {
              forAll(
                arbitrary[CustomsOffice](arbitraryXiCustomsOffice),
                arbitrary[String](arbitraryDeclarationType),
                arbitrary[Boolean]
              ) {
                (xiCustomsOffice, declarationType, attachToAllItems) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationOfficeOfDeparturePage, xiCustomsOffice)
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(AttachToAllItemsPage(Index(0)), attachToAllItems)

                  val helper = new DocumentsAnswersHelper(userAnswers)

                  helper.listItems mustBe Seq(
                    Left(
                      ListItem(
                        name = "",
                        changeUrl = controllers.document.routes.TypeController.onPageLoad(userAnswers.lrn, NormalMode, Index(0)).url,
                        removeUrl = Some(routes.RemoveDocumentController.onPageLoad(lrn, Index(0)).url)
                      )
                    )
                  )
              }
            }
          }

          "when Previous Type page is unpopulated" - {
            "must return list items with remove links" in {
              forAll(
                arbitrary[CustomsOffice](arbitraryGbCustomsOffice),
                arbitrary[String](arbitraryT2OrT2FDeclarationType),
                arbitrary[Boolean]
              ) {
                (gbCustomsOffice, declarationType, attachToAllItems) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(TransitOperationOfficeOfDeparturePage, gbCustomsOffice)
                    .setValue(AttachToAllItemsPage(Index(0)), attachToAllItems)

                  val helper = new DocumentsAnswersHelper(userAnswers)

                  helper.listItems mustBe Seq(
                    Left(
                      ListItem(
                        name = "",
                        changeUrl = controllers.document.routes.PreviousDocumentTypeController.onPageLoad(userAnswers.lrn, NormalMode, Index(0)).url,
                        removeUrl = None
                      )
                    )
                  )
              }
            }
          }

          "when Type page is populated" - {
            "must return list items with remove links" in {
              forAll(
                arbitrary[CustomsOffice](arbitraryXiCustomsOffice),
                arbitrary[String](arbitraryDeclarationType),
                arbitrary[Boolean],
                arbitrary[Document]
              ) {
                (xiCustomsOffice, declarationType, attachToAllItems, document) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationOfficeOfDeparturePage, xiCustomsOffice)
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(AttachToAllItemsPage(Index(0)), attachToAllItems)
                    .setValue(TypePage(Index(0)), document)

                  val helper = new DocumentsAnswersHelper(userAnswers)

                  helper.listItems mustBe Seq(
                    Left(
                      ListItem(
                        name = document.toString,
                        changeUrl = controllers.document.routes.DocumentReferenceNumberController.onPageLoad(userAnswers.lrn, NormalMode, Index(0)).url,
                        removeUrl = Some(routes.RemoveDocumentController.onPageLoad(lrn, Index(0)).url)
                      )
                    )
                  )
              }
            }
          }

          "when Previous Type page is populated" - {
            "must return list items with remove links" in {
              forAll(
                arbitrary[CustomsOffice](arbitraryGbCustomsOffice),
                arbitrary[String](arbitraryT2OrT2FDeclarationType),
                arbitrary[Boolean],
                arbitrary[Document](arbitraryPreviousDocument)
              ) {
                (gbCustomsOffice, declarationType, attachToAllItems, previousDocument) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(TransitOperationOfficeOfDeparturePage, gbCustomsOffice)
                    .setValue(AttachToAllItemsPage(Index(0)), attachToAllItems)
                    .setValue(PreviousDocumentTypePage(Index(0)), previousDocument)

                  val helper = new DocumentsAnswersHelper(userAnswers)

                  helper.listItems mustBe Seq(
                    Left(
                      ListItem(
                        name = previousDocument.toString,
                        changeUrl = controllers.document.routes.DocumentReferenceNumberController.onPageLoad(userAnswers.lrn, NormalMode, Index(0)).url,
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
