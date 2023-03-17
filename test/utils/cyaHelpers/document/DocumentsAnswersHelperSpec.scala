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

package utils.cyaHelpers.document

import base.SpecBase
import generators.Generators
import models.DeclarationType._
import models.reference.{Document, PackageType}
import models.{Index, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}
import play.api.mvc.Call
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
              arbitrary[Document],
              nonEmptyString,
              nonEmptyString,
              arbitrary[PackageType],
              Gen.oneOf(T1, TIR, T),
              arbitraryXiCustomsOffice
            ) {
              (document, referenceNumber, goodsItemNumber, packageType, declarationType, xiCustomsOffice) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, xiCustomsOffice.arbitrary.sample.get)
                  .setValue(TypePage(Index(0)), document)
                  .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)
                  .setValue(AddGoodsItemNumberYesNoPage(Index(0)), true)
                  .setValue(GoodsItemNumberPage(Index(0)), goodsItemNumber)
                  .setValue(AddTypeOfPackageYesNoPage(Index(0)), true)
                  .setValue(PackageTypePage(Index(0)), packageType)

                val helper = new DocumentsAnswersHelper(userAnswers, NormalMode)

                helper.listItems mustBe Seq(
                  Right(
                    ListItem(
                      name = s"${document.toString} - $referenceNumber",
                      changeUrl = Call("GET", "#").url, //TODO - change to documents CYA page when built
                      removeUrl = Some(Call("GET", "#").url) //TODO - change to remove controller when built
                    )
                  )
                )
            }
          }

          "when Previous Type page is populated" - {
            "must return list items with remove links" in {
              forAll(arbitrary[Document](arbitraryPreviousDocument),
                     nonEmptyString,
                     nonEmptyString,
                     arbitrary[PackageType],
                     Gen.oneOf(T2, T2F),
                     arbitraryGbCustomsOffice
              ) {
                (previousDocument, referenceNumber, goodsItemNumber, packageType, declarationType, gbCustomsOffice) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(TransitOperationOfficeOfDeparturePage, gbCustomsOffice.arbitrary.sample.get)
                    .setValue(PreviousDocumentTypePage(Index(0)), previousDocument)
                    .setValue(DocumentReferenceNumberPage(Index(0)), referenceNumber)
                    .setValue(AddGoodsItemNumberYesNoPage(Index(0)), true)
                    .setValue(GoodsItemNumberPage(Index(0)), goodsItemNumber)
                    .setValue(AddTypeOfPackageYesNoPage(Index(0)), true)
                    .setValue(PackageTypePage(Index(0)), packageType)

                  val helper = new DocumentsAnswersHelper(userAnswers, NormalMode)

                  helper.listItems mustBe Seq(
                    Right(
                      ListItem(
                        name = s"${previousDocument.toString} - $referenceNumber",
                        changeUrl = Call("GET", "#").url, //TODO - change to documents CYA page when built
                        removeUrl = Some(Call("GET", "#").url) //TODO - change to remove controller when built
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
                arbitrary[Document],
                Gen.oneOf(T1, TIR, T),
                arbitraryXiCustomsOffice
              ) {
                (document, declarationType, xiCustomsOffice) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(TransitOperationOfficeOfDeparturePage, xiCustomsOffice.arbitrary.sample.get)
                    .setValue(TypePage(Index(0)), document)

                  val helper = new DocumentsAnswersHelper(userAnswers, NormalMode)

                  helper.listItems mustBe Seq(
                    Left(
                      ListItem(
                        name = s"${document.toString}",
                        changeUrl = controllers.document.routes.DocumentReferenceNumberController.onPageLoad(userAnswers.lrn, NormalMode, Index(0)).url,
                        removeUrl = Some(Call("GET", "#").url) //TODO - change to remove controller when built
                      )
                    )
                  )
              }
            }
          }

          "when Previous Type page is populated" - {
            "must return list items with remove links" in {
              forAll(
                arbitrary[Document](arbitraryPreviousDocument),
                Gen.oneOf(T2, T2F),
                arbitraryGbCustomsOffice
              ) {
                (previousDocument, declarationType, gbCustomsOffice) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(TransitOperationDeclarationTypePage, declarationType)
                    .setValue(TransitOperationOfficeOfDeparturePage, gbCustomsOffice.arbitrary.sample.get)
                    .setValue(PreviousDocumentTypePage(Index(0)), previousDocument)

                  val helper = new DocumentsAnswersHelper(userAnswers, NormalMode)

                  helper.listItems mustBe Seq(
                    Left(
                      ListItem(
                        name = s"${previousDocument.toString}",
                        changeUrl = controllers.document.routes.DocumentReferenceNumberController.onPageLoad(userAnswers.lrn, NormalMode, Index(0)).url,
                        removeUrl = Some(Call("GET", "#").url) //TODO - change to remove controller when built
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
