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
import models.DeclarationType._
import models.DocumentType._
import models.Index
import models.reference.{CustomsOffice, Document, PackageType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._
import pages.external._

class DocumentDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Document Domain" - {

    "userAnswersReader" - {

      "can not be read from user answers" - {
        "when index is 0" - {
          val index = Index(0)
          "and Declaration Type is in set T2/T2F and Office of Departure is in GB" in {
            val declarationTypeGen   = Gen.oneOf(T2, T2F)
            val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
            forAll(declarationTypeGen, officeOfDepartureGen) {
              (declarationType, officeOfDeparture) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)

                val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                  DocumentDomain.userAnswersReader(index)
                ).run(userAnswers)

                result.left.value.page mustBe PreviousDocumentTypePage(index)
            }
          }

          "and Declaration Type is not in set T2/T2F" in {
            val declarationTypeGen   = Gen.oneOf(T1, TIR, T)
            val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
            forAll(declarationTypeGen, officeOfDepartureGen) {
              (declarationType, officeOfDeparture) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)

                val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                  DocumentDomain.userAnswersReader(index)
                ).run(userAnswers)

                result.left.value.page mustBe TypePage(index)
            }
          }

          "and Office of Departure is not in GB" in {
            val declarationTypeGen   = Gen.oneOf(T2, T2F)
            val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryXiCustomsOffice)
            forAll(declarationTypeGen, officeOfDepartureGen) {
              (declarationType, officeOfDeparture) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)

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
            forAll(declarationTypeGen, officeOfDepartureGen) {
              (declarationType, officeOfDeparture) =>
                val userAnswers = emptyUserAnswers
                  .setValue(TransitOperationDeclarationTypePage, declarationType)
                  .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)

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
        forAll(documentGen, nonEmptyString, positiveInts) {
          (document, referenceNumber, lineItemNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), true)
              .setValue(LineItemNumberPage(index), lineItemNumber)

            val expectedResult = SupportDocumentDomain(
              document = document,
              referenceNumber = referenceNumber,
              lineItemNumber = Some(lineItemNumber)
            )(index)

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.value mustBe expectedResult
        }
      }

      "when line item number undefined" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), false)

            val expectedResult = SupportDocumentDomain(
              document = document,
              referenceNumber = referenceNumber,
              lineItemNumber = None
            )(index)

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.value mustBe expectedResult
        }
      }
    }

    "can not be read from user answers" - {
      "when reference number is unanswered" in {
        forAll(documentGen) {
          document =>
            val userAnswers = emptyUserAnswers

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe DocumentReferenceNumberPage(index)
        }
      }

      "when add line item number yes/no is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe AddLineItemNumberYesNoPage(index)
        }
      }

      "when line item number is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddLineItemNumberYesNoPage(index), true)

            val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
              SupportDocumentDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe LineItemNumberPage(index)
        }
      }
    }
  }

  "TransportDocumentDomain userAnswersReader" - {
    val documentGen = arbitrary[Document](arbitraryTransportDocument)

    "can be read from user answers" in {
      forAll(documentGen, nonEmptyString) {
        (document, referenceNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(DocumentReferenceNumberPage(index), referenceNumber)

          val expectedResult = TransportDocumentDomain(
            document = document,
            referenceNumber = referenceNumber
          )(index)

          val result: EitherType[TransportDocumentDomain] = UserAnswersReader[TransportDocumentDomain](
            TransportDocumentDomain.userAnswersReader(index, document)
          ).run(userAnswers)

          result.value mustBe expectedResult
      }
    }
  }

  "PreviousDocumentDomain userAnswersReader" - {
    val documentGen = arbitrary[Document](arbitraryPreviousDocument)

    "can be read from user answers" in {
      forAll(documentGen, nonEmptyString, nonEmptyString, arbitrary[PackageType]) {
        (document, referenceNumber, goodsItemNumber, packageType) =>
          val userAnswers = emptyUserAnswers
            .setValue(DocumentReferenceNumberPage(index), referenceNumber)
            .setValue(AddGoodsItemNumberYesNoPage(index), true)
            .setValue(GoodsItemNumberPage(index), goodsItemNumber)
            .setValue(AddTypeOfPackageYesNoPage(index), true)
            .setValue(PackageTypePage(index), packageType)

          val expectedResult = PreviousDocumentDomain(
            document = document,
            referenceNumber = referenceNumber,
            goodsItemNumber = Some(goodsItemNumber),
            `package` = Some(
              PackageDomain(
                `type` = packageType,
                numberOfPackages = 0
              )
            )
          )(index)

          val result: EitherType[PreviousDocumentDomain] = UserAnswersReader[PreviousDocumentDomain](
            PreviousDocumentDomain.userAnswersReader(index, document)
          ).run(userAnswers)

          result.value mustBe expectedResult
      }
    }

    "can not be read from user answers" - {
      "when add goods item number yes/no is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)

            val result: EitherType[PreviousDocumentDomain] = UserAnswersReader[PreviousDocumentDomain](
              PreviousDocumentDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe AddGoodsItemNumberYesNoPage(index)
        }
      }

      "when goods item number is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddGoodsItemNumberYesNoPage(index), true)

            val result: EitherType[PreviousDocumentDomain] = UserAnswersReader[PreviousDocumentDomain](
              PreviousDocumentDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe GoodsItemNumberPage(index)
        }
      }

      "when add package yes/no is unanswered" in {
        forAll(documentGen, nonEmptyString) {
          (document, referenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(DocumentReferenceNumberPage(index), referenceNumber)
              .setValue(AddGoodsItemNumberYesNoPage(index), false)

            val result: EitherType[PreviousDocumentDomain] = UserAnswersReader[PreviousDocumentDomain](
              PreviousDocumentDomain.userAnswersReader(index, document)
            ).run(userAnswers)

            result.left.value.page mustBe AddTypeOfPackageYesNoPage(index)
        }
      }
    }
  }
}
