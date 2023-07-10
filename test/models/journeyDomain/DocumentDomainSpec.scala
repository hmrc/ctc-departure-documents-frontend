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
import generators.{ConsignmentLevelDocumentsGenerator, Generators}
import models.DeclarationType._
import models.DocumentType._
import models.reference.{CustomsOffice, Document, Metric, PackageType}
import models.{DeclarationType, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._
import pages.external._
import pages.sections.DocumentSection
import play.api.libs.json.Json

class DocumentDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with ConsignmentLevelDocumentsGenerator {

  "Document Domain" - {

    "userAnswersReader" - {

      "can not be read from user answers" - {
        "when attach to all items is unanswered" in {
          val userAnswers = emptyUserAnswers
            .setValue(TransitOperationOfficeOfDeparturePage, arbitrary[CustomsOffice].sample.value)
            .setValue(TransitOperationDeclarationTypePage, arbitrary[DeclarationType].sample.value)

          val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
            DocumentDomain.userAnswersReader(index)
          ).run(userAnswers)

          result.left.value.page mustBe AttachToAllItemsPage(index)
        }

        "when attach to all items is inferred as false because we cannot add any more consignment level documents" in {
          val nextIndex = Index(numberOfDocuments)

          val userAnswers = userAnswersWithConsignmentLevelDocumentsMaxedOut
            .setValue(InferredAttachToAllItemsPage(nextIndex), false)

          val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
            DocumentDomain.userAnswersReader(nextIndex)
          ).run(userAnswers)

          result.left.value.page mustBe TypePage(nextIndex)
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

                val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                  DocumentDomain.userAnswersReader(index)
                ).run(userAnswers)

                result.left.value.page mustBe PreviousDocumentTypePage(index)
            }
          }

          "and Declaration Type is not in set T2/T2F" in {
            val declarationTypeGen   = Gen.oneOf(T1, TIR, T)
            val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
            forAll(declarationTypeGen, officeOfDepartureGen, arbitrary[Boolean]) {
              (declarationType, officeOfDeparture, attachToAllItems) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                  .setValue(AttachToAllItemsPage(index), attachToAllItems)

                val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                  DocumentDomain.userAnswersReader(index)
                ).run(userAnswers)

                result.left.value.page mustBe TypePage(index)
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

                val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                  DocumentDomain.userAnswersReader(index)
                ).run(userAnswers)

                result.left.value.page mustBe TypePage(index)
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

                val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                  DocumentDomain.userAnswersReader(index)
                ).run(userAnswers)

                result.left.value.page mustBe TypePage(index)
            }
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

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.value mustBe expectedResult
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

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.value mustBe expectedResult
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

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.value mustBe expectedResult
        }
      }
    }

    "can not be read from user answers" - {
      "when reference number is unanswered" in {
        forAll(documentGen, arbitrary[Boolean]) {
          (document, attachToAllItems) =>
            val userAnswers = emptyUserAnswers

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.left.value.page mustBe DocumentReferenceNumberPage(index)
        }
      }

      "when add line item number yes/no is unanswered" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.left.value.page mustBe AddLineItemNumberYesNoPage(index)
        }
      }

      "when line item number is unanswered" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), true)

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.left.value.page mustBe LineItemNumberPage(index)
        }
      }

      "when additional information yes/no is unanswered" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), false)

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.left.value.page mustBe AddAdditionalInformationYesNoPage(index)
        }
      }

      "when additional information is unanswered" in {
        forAll(documentGen, arbitrary[Boolean], nonEmptyString) {
          (document, attachToAllItems, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), false)
              .setValue(AddAdditionalInformationYesNoPage(index), true)

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.left.value.page mustBe AdditionalInformationPage(index)
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

            val result: EitherType[TransportDocumentDomain] = UserAnswersReader[TransportDocumentDomain](
              TransportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.value mustBe expectedResult
        }
      }
    }

    "can not be read from user answers" - {
      "when reference number is unanswered" in {
        forAll(documentGen, arbitrary[Boolean]) {
          (document, attachToAllItems) =>
            val userAnswers = emptyUserAnswers

            val result: EitherType[TransportDocumentDomain] = UserAnswersReader[TransportDocumentDomain](
              TransportDocumentDomain.userAnswersReader(index, attachToAllItems, document)
            ).run(userAnswers)

            result.left.value.page mustBe DocumentReferenceNumberPage(index)
        }
      }
    }
  }

  "PreviousDocumentItemLevelDomain userAnswersReader" - {
    val documentGen = arbitrary[Document](arbitraryPreviousDocument)

    "can be read from user answers" in {
      forAll(documentGen, nonEmptyString, arbitrary[PackageType], arbitrary[Metric], arbitrary[BigDecimal]) {
        (document, referenceInformation, packageType, metric, quantity) =>
          val userAnswers = emptyUserAnswers
            .setValue(DocumentReferenceNumberPage(index), referenceInformation)
            .setValue(AddTypeOfPackageYesNoPage(index), true)
            .setValue(PackageTypePage(index), packageType)
            .setValue(AddNumberOfPackagesYesNoPage(index), false)
            .setValue(DeclareQuantityOfGoodsYesNoPage(index), true)
            .setValue(MetricPage(index), metric)
            .setValue(QuantityPage(index), quantity)
            .setValue(AddAdditionalInformationYesNoPage(index), true)
            .setValue(AdditionalInformationPage(index), referenceInformation)

          val expectedResult = PreviousDocumentItemLevelDomain(
            document = document,
            referenceNumber = referenceInformation,
            `package` = Some(
              PackageDomain(
                `type` = packageType,
                numberOfPackages = None
              )
            ),
            quantity = Some(
              QuantityDomain(
                metric = metric,
                value = quantity
              )
            ),
            additionalInformation = Some(referenceInformation)
          )(index)

          val result: EitherType[PreviousDocumentItemLevelDomain] = UserAnswersReader[PreviousDocumentItemLevelDomain](
            PreviousDocumentItemLevelDomain.userAnswersReader(index, document)
          ).run(userAnswers)

          result.value mustBe expectedResult
      }
    }

    "can not be read from user answers" - {
      "when reference number is unanswered" in {
        forAll(documentGen) {
          document =>
            val userAnswers = emptyUserAnswers

            val result: EitherType[PreviousDocumentItemLevelDomain] = UserAnswersReader[PreviousDocumentItemLevelDomain](
              PreviousDocumentItemLevelDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe DocumentReferenceNumberPage(index)
        }
      }

      "when add package yes/no is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)

            val result: EitherType[PreviousDocumentItemLevelDomain] = UserAnswersReader[PreviousDocumentItemLevelDomain](
              PreviousDocumentItemLevelDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe AddTypeOfPackageYesNoPage(index)
        }
      }

      "when add quantity yes/no is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddTypeOfPackageYesNoPage(index), false)

            val result: EitherType[PreviousDocumentItemLevelDomain] = UserAnswersReader[PreviousDocumentItemLevelDomain](
              PreviousDocumentItemLevelDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe DeclareQuantityOfGoodsYesNoPage(index)
        }
      }

      "when additional information yes/no is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddTypeOfPackageYesNoPage(index), false)
              .setValue(DeclareQuantityOfGoodsYesNoPage(index), false)

            val result: EitherType[PreviousDocumentItemLevelDomain] = UserAnswersReader[PreviousDocumentItemLevelDomain](
              PreviousDocumentItemLevelDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe AddAdditionalInformationYesNoPage(index)
        }
      }

      "when additional information is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddTypeOfPackageYesNoPage(index), false)
              .setValue(DeclareQuantityOfGoodsYesNoPage(index), false)
              .setValue(AddAdditionalInformationYesNoPage(index), true)

            val result: EitherType[PreviousDocumentItemLevelDomain] = UserAnswersReader[PreviousDocumentItemLevelDomain](
              PreviousDocumentItemLevelDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe AdditionalInformationPage(index)
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

          val result: EitherType[PreviousDocumentConsignmentLevelDomain] = UserAnswersReader[PreviousDocumentConsignmentLevelDomain](
            PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document)
          ).run(userAnswers)

          result.value mustBe expectedResult
      }
    }

    "can not be read from user answers" - {
      "when reference number is unanswered" in {
        forAll(documentGen) {
          document =>
            val userAnswers = emptyUserAnswers

            val result: EitherType[PreviousDocumentConsignmentLevelDomain] = UserAnswersReader[PreviousDocumentConsignmentLevelDomain](
              PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe DocumentReferenceNumberPage(index)
        }
      }

      "when additional information yes/no is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)

            val result: EitherType[PreviousDocumentConsignmentLevelDomain] = UserAnswersReader[PreviousDocumentConsignmentLevelDomain](
              PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe AddAdditionalInformationYesNoPage(index)
        }
      }

      "when additional information is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddAdditionalInformationYesNoPage(index), true)

            val result: EitherType[PreviousDocumentConsignmentLevelDomain] = UserAnswersReader[PreviousDocumentConsignmentLevelDomain](
              PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe AdditionalInformationPage(index)
        }
      }
    }
  }
}
