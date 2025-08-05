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

package models.journeyDomain

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.Constants.DeclarationType.*
import generators.{ConsignmentLevelDocumentsGenerator, Generators}
import models.DocumentType.*
import models.Index
import models.reference.{CustomsOffice, Document}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.*
import pages.external.*
import pages.sections.DocumentSection
import play.api.libs.json.Json

class DocumentDomainSpec
    extends SpecBase
    with AppWithDefaultMockFixtures
    with ScalaCheckPropertyChecks
    with Generators
    with ConsignmentLevelDocumentsGenerator {

  "Document Domain" - {

    "userAnswersReader" - {

      "can not be read from user answers" - {
        "when attach to all items is unanswered" in {
          val userAnswers = emptyUserAnswers
            .setValue(TransitOperationOfficeOfDeparturePage, arbitrary[CustomsOffice].sample.value)
            .setValue(TransitOperationDeclarationTypePage, arbitrary[String](arbitraryDeclarationType).sample.value)

          val result = DocumentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.left.value.page mustEqual AttachToAllItemsPage(index)
          result.left.value.pages mustEqual Seq(
            AttachToAllItemsPage(index)
          )
        }

        "when attach to all items is inferred as false because we cannot add any more consignment level documents" in {
          val nextIndex = Index(numberOfDocuments)

          val userAnswers = userAnswersWithConsignmentLevelDocumentsMaxedOut
            .setValue(InferredAttachToAllItemsPage(nextIndex), false)

          val result = DocumentDomain.userAnswersReader(nextIndex).apply(Nil).run(userAnswers)

          result.left.value.page mustEqual TypePage(nextIndex)
          result.left.value.pages mustEqual Seq(
            TypePage(nextIndex)
          )
        }

        "when we have redirected from items and added a mandatory previous item level document type" in {
          forAll(arbitrary[CustomsOffice](arbitraryGbCustomsOffice), arbitrary[Document](arbitraryPreviousDocument)) {
            (customsOffice, document) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationOfficeOfDeparturePage, customsOffice)
                .setValue(TransitOperationDeclarationTypePage, T)
                .setValue(InferredAttachToAllItemsPage(index), false)
                .setValue(PreviousDocumentTypePage(index), document)

              val result = DocumentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

              result.left.value.page mustEqual DocumentReferenceNumberPage(index)
              result.left.value.pages mustEqual Seq(
                PreviousDocumentTypePage(index),
                DocumentReferenceNumberPage(index)
              )
          }
        }

        "when index is 0" - {
          val index = Index(0)
          "and Declaration Type is in set T2/T2F and Office of Departure is in GB" in {
            val declarationTypeGen   = Gen.oneOf(T2, T2F)
            val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
            forAll(declarationTypeGen, officeOfDepartureGen, arbitrary[Boolean]) {
              (declarationType, officeOfDeparture, attachToAllItems) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                  .setValue(AttachToAllItemsPage(index), attachToAllItems)

                val result = DocumentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

                result.left.value.page mustEqual PreviousDocumentTypePage(index)
                result.left.value.pages mustEqual Seq(
                  AttachToAllItemsPage(index),
                  PreviousDocumentTypePage(index)
                )
            }
          }

          "and Declaration Type is not in set T2/T2F" in {
            val declarationTypeGen   = arbitrary[String](arbitraryNonT2OrT2FDeclarationType)
            val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
            forAll(declarationTypeGen, officeOfDepartureGen, arbitrary[Boolean]) {
              (declarationType, officeOfDeparture, attachToAllItems) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                  .setValue(AttachToAllItemsPage(index), attachToAllItems)

                val result = DocumentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

                result.left.value.page mustEqual TypePage(index)
                result.left.value.pages mustEqual Seq(
                  AttachToAllItemsPage(index),
                  TypePage(index)
                )
            }
          }

          "and Office of Departure is not in GB" in {
            val declarationTypeGen   = Gen.oneOf(T2, T2F)
            val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryXiCustomsOffice)
            forAll(declarationTypeGen, officeOfDepartureGen, arbitrary[Boolean]) {
              (declarationType, officeOfDeparture, attachToAllItems) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                  .setValue(AttachToAllItemsPage(index), attachToAllItems)

                val result = DocumentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

                result.left.value.page mustEqual TypePage(index)
                result.left.value.pages mustEqual Seq(
                  AttachToAllItemsPage(index),
                  TypePage(index)
                )
            }
          }
        }

        "when index is not 0" - {
          val index = Index(1)
          "and Declaration Type is in set T2/T2F and Office of Departure is in GB" in {
            val declarationTypeGen   = Gen.oneOf(T2, T2F)
            val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
            forAll(declarationTypeGen, officeOfDepartureGen, arbitrary[Boolean]) {
              (declarationType, officeOfDeparture, attachToAllItems) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                  .setValue(DocumentSection(Index(0)), Json.obj("foo" -> "bar"))
                  .setValue(AttachToAllItemsPage(index), attachToAllItems)

                val result = DocumentDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

                result.left.value.page mustEqual TypePage(index)
                result.left.value.pages mustEqual Seq(
                  AttachToAllItemsPage(index),
                  TypePage(index)
                )
            }
          }
        }
      }
    }

    "asString" - {

      val document        = Document(Previous, "N270", "Delivery note")
      val referenceNumber = "123"

      "must format document as string" - {
        "when document type and ref. number defined" in {
          val result = DocumentDomain.asString(index, Some(document), Some(referenceNumber))
          result mustEqual "Previous - (N270) Delivery note - 123"
        }

        "when only document type defined" in {
          val result = DocumentDomain.asString(index, Some(document), None)
          result mustEqual "Previous - (N270) Delivery note"
        }

        "when neither document type nor ref. number defined" in {
          forAll(arbitrary[Index]) {
            index =>
              val result = DocumentDomain.asString(index, None, None)
              result mustEqual s"${index.display}"
          }
        }
      }
    }
  }

  "SupportDocumentDomain userAnswersReader" - {
    val documentGen = arbitrary[Document](arbitrarySupportDocument)

    "can be read from user answers" - {
      "when line item number defined" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString, positiveInts, nonEmptyString) {
          (document, attachToAllItems, referenceNumber, lineItemNumber, additionalInformation) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), true)
              .setValue(LineItemNumberPage(index), lineItemNumber)
              .setValue(AddAdditionalInformationYesNoPage(index), true)
              .setValue(AdditionalInformationPage(index), additionalInformation)

            val expectedResult = SupportDocumentDomain(
              document = document,
              attachToAllItems = attachToAllItems,
              referenceNumber = referenceNumber,
              lineItemNumber = Some(lineItemNumber),
              additionalInformation = Some(additionalInformation)
            )(index)

            val result = SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.value.value mustEqual expectedResult
            result.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddLineItemNumberYesNoPage(index),
              LineItemNumberPage(index),
              AddAdditionalInformationYesNoPage(index),
              AdditionalInformationPage(index),
              DocumentSection(index)
            )
        }
      }

      "when line item number is undefined" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString, nonEmptyString) {
          (document, attachToAllItems, referenceNumber, additionalInformation) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), false)
              .setValue(AddAdditionalInformationYesNoPage(index), true)
              .setValue(AdditionalInformationPage(index), additionalInformation)

            val expectedResult = SupportDocumentDomain(
              document = document,
              attachToAllItems = attachToAllItems,
              referenceNumber = referenceNumber,
              lineItemNumber = None,
              additionalInformation = Some(additionalInformation)
            )(index)

            val result = SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.value.value mustEqual expectedResult
            result.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddLineItemNumberYesNoPage(index),
              AddAdditionalInformationYesNoPage(index),
              AdditionalInformationPage(index),
              DocumentSection(index)
            )
        }
      }

      "when additional information is undefined" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), false)
              .setValue(AddAdditionalInformationYesNoPage(index), false)

            val expectedResult = SupportDocumentDomain(
              document = document,
              attachToAllItems = attachToAllItems,
              referenceNumber = referenceNumber,
              lineItemNumber = None,
              additionalInformation = None
            )(index)

            val result = SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.value.value mustEqual expectedResult
            result.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddLineItemNumberYesNoPage(index),
              AddAdditionalInformationYesNoPage(index),
              DocumentSection(index)
            )
        }
      }
    }

    "can not be read from user answers" - {
      "when reference number is unanswered" in {
        forAll(documentGen, arbitrary[Boolean]) {
          (document, attachToAllItems) =>
            val userAnswers = emptyUserAnswers

            val result = SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual DocumentReferenceNumberPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index)
            )
        }
      }

      "when add line item number yes/no is unanswered" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)

            val result = SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual AddLineItemNumberYesNoPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddLineItemNumberYesNoPage(index)
            )
        }
      }

      "when line item number is unanswered" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), true)

            val result = SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual LineItemNumberPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddLineItemNumberYesNoPage(index),
              LineItemNumberPage(index)
            )
        }
      }

      "when additional information yes/no is unanswered" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), false)

            val result = SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual AddAdditionalInformationYesNoPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddLineItemNumberYesNoPage(index),
              AddAdditionalInformationYesNoPage(index)
            )
        }
      }

      "when additional information is unanswered" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), false)
              .setValue(AddAdditionalInformationYesNoPage(index), true)

            val result = SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual AdditionalInformationPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddLineItemNumberYesNoPage(index),
              AddAdditionalInformationYesNoPage(index),
              AdditionalInformationPage(index)
            )
        }
      }
    }
  }

  "TransportDocumentDomain userAnswersReader" - {
    val documentGen = arbitrary[Document](arbitraryTransportDocument)

    "can be read from user answers" - {
      "when all questions answered" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)

            val expectedResult = TransportDocumentDomain(
              attachToAllItems = attachToAllItems,
              document = document,
              referenceNumber = referenceNumber
            )(index)

            val result = TransportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.value.value mustEqual expectedResult
            result.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              DocumentSection(index)
            )
        }
      }
    }

    "can not be read from user answers" - {
      "when reference number is unanswered" in {
        forAll(documentGen, arbitrary[Boolean]) {
          (document, attachToAllItems) =>
            val userAnswers = emptyUserAnswers

            val result = TransportDocumentDomain.userAnswersReader(index, attachToAllItems, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual DocumentReferenceNumberPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index)
            )
        }
      }
    }
  }

  "PreviousDocumentItemLevelDomain userAnswersReader" - {
    val documentGen = arbitrary[Document](arbitraryPreviousDocument)

    "can be read from user answers" in {
      forAll(documentGen, nonEmptyString, arbitrary[BigDecimal]) {
        (document, referenceInformation, quantity) =>
          val userAnswers = emptyUserAnswers
            .setValue(DocumentReferenceNumberPage(index), referenceInformation)
            .setValue(AddAdditionalInformationYesNoPage(index), true)
            .setValue(AdditionalInformationPage(index), referenceInformation)

          val expectedResult = PreviousDocumentItemLevelDomain(
            document = document,
            referenceNumber = referenceInformation,
            additionalInformation = Some(referenceInformation)
          )(index)

          val result = PreviousDocumentItemLevelDomain.userAnswersReader(index, document).apply(Nil).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            DocumentReferenceNumberPage(index),
            AddAdditionalInformationYesNoPage(index),
            AdditionalInformationPage(index),
            DocumentSection(index)
          )
      }
    }

    "can not be read from user answers" - {
      "when reference number is unanswered" in {
        forAll(documentGen) {
          document =>
            val userAnswers = emptyUserAnswers

            val result = PreviousDocumentItemLevelDomain.userAnswersReader(index, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual DocumentReferenceNumberPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index)
            )
        }
      }

      "when additional information yes/no is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)

            val result = PreviousDocumentItemLevelDomain.userAnswersReader(index, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual AddAdditionalInformationYesNoPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddAdditionalInformationYesNoPage(index)
            )
        }
      }

      "when additional information is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddAdditionalInformationYesNoPage(index), true)

            val result = PreviousDocumentItemLevelDomain.userAnswersReader(index, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual AdditionalInformationPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddAdditionalInformationYesNoPage(index),
              AdditionalInformationPage(index)
            )
        }
      }
    }
  }

  "PreviousDocumentConsignmentLevelDomain userAnswersReader" - {
    val documentGen = arbitrary[Document](arbitraryPreviousDocument)

    "can be read from user answers" in {
      forAll(documentGen, nonEmptyString, nonEmptyString) {
        (document, referenceNumber, additionalInformation) =>
          val userAnswers = emptyUserAnswers
            .setValue(DocumentReferenceNumberPage(index), referenceNumber)
            .setValue(AddAdditionalInformationYesNoPage(index), true)
            .setValue(AdditionalInformationPage(index), additionalInformation)

          val expectedResult = PreviousDocumentConsignmentLevelDomain(
            document = document,
            referenceNumber = referenceNumber,
            additionalInformation = Some(additionalInformation)
          )(index)

          val result = PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document).apply(Nil).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            DocumentReferenceNumberPage(index),
            AddAdditionalInformationYesNoPage(index),
            AdditionalInformationPage(index),
            DocumentSection(index)
          )
      }
    }

    "can not be read from user answers" - {
      "when reference number is unanswered" in {
        forAll(documentGen) {
          document =>
            val userAnswers = emptyUserAnswers

            val result = PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual DocumentReferenceNumberPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index)
            )
        }
      }

      "when additional information yes/no is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)

            val result = PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual AddAdditionalInformationYesNoPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddAdditionalInformationYesNoPage(index)
            )
        }
      }

      "when additional information is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddAdditionalInformationYesNoPage(index), true)

            val result = PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual AdditionalInformationPage(index)
            result.left.value.pages mustEqual Seq(
              DocumentReferenceNumberPage(index),
              AddAdditionalInformationYesNoPage(index),
              AdditionalInformationPage(index)
            )
        }
      }
    }
  }
}
