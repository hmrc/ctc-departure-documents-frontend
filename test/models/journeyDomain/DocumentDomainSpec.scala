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
import models.reference.{CustomsOffice, Document}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.{DocumentReferenceNumberPage, PreviousDocumentTypePage, TypePage}
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}

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

    "can be read from user answers" in {
      val documentGen = arbitrary[Document](arbitrarySupportDocument)
      forAll(documentGen, nonEmptyString) {
        (document, referenceNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(DocumentReferenceNumberPage(index), referenceNumber)

          val expectedResult = SupportDocumentDomain(
            document = document,
            referenceNumber = referenceNumber
          )(index)

          val result: EitherType[SupportDocumentDomain] = UserAnswersReader[SupportDocumentDomain](
            SupportDocumentDomain.userAnswersReader(index, document)
          ).run(userAnswers)

          result.value mustBe expectedResult
      }
    }
  }

  "TransportDocumentDomain userAnswersReader" - {

    "can be read from user answers" in {
      val documentGen = arbitrary[Document](arbitraryTransportDocument)
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

    "can be read from user answers" in {
      val documentGen = arbitrary[Document](arbitraryPreviousDocument)
      forAll(documentGen, nonEmptyString) {
        (document, referenceNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(DocumentReferenceNumberPage(index), referenceNumber)

          val expectedResult = PreviousDocumentDomain(
            document = document,
            referenceNumber = referenceNumber
          )(index)

          val result: EitherType[PreviousDocumentDomain] = UserAnswersReader[PreviousDocumentDomain](
            PreviousDocumentDomain.userAnswersReader(index, document)
          ).run(userAnswers)

          result.value mustBe expectedResult
      }
    }
  }
}
