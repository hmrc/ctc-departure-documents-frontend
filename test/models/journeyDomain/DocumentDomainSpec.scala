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
import models.reference.{CustomsOffice, Document}
import models.{DeclarationType, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.{PreviousDocumentTypePage, TypePage}
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}
import pages.sections.DocumentSection
import play.api.libs.json.Json

class DocumentDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Document Domain" - {

    "can be read from user answers" - {
      "when index is 0" - {
        val index = Index(0)
        "and mandatory previous document type" in {
          val declarationTypeGen   = Gen.oneOf(T2, T2F)
          val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
          val documentGen          = arbitrary[Document](arbitraryPreviousDocument)
          forAll(declarationTypeGen, officeOfDepartureGen, documentGen) {
            (declarationType, officeOfDeparture, document) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationDeclarationTypePage, declarationType)
                .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                .setValue(PreviousDocumentTypePage(index), document)

              val expectedResult = PreviousDocumentDomain(
                document = document
              )

              val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                DocumentDomain.userAnswersReader(index)
              ).run(userAnswers)

              result.value mustBe expectedResult
          }
        }
      }

      "when index is not 0" - {
        val index = Index(1)
        "and previous document type" in {
          val declarationTypeGen   = arbitrary[DeclarationType]
          val officeOfDepartureGen = arbitrary[CustomsOffice]
          val documentGen          = arbitrary[Document](arbitraryPreviousDocument)
          forAll(declarationTypeGen, officeOfDepartureGen, documentGen) {
            (declarationType, officeOfDeparture, document) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationDeclarationTypePage, declarationType)
                .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                .setValue(DocumentSection(Index(0)), Json.obj("foo" -> "bar"))
                .setValue(TypePage(index), document)

              val expectedResult = PreviousDocumentDomain(
                document = document
              )

              val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                DocumentDomain.userAnswersReader(index)
              ).run(userAnswers)

              result.value mustBe expectedResult
          }
        }

        "and support document type" in {
          val declarationTypeGen   = arbitrary[DeclarationType]
          val officeOfDepartureGen = arbitrary[CustomsOffice]
          val documentGen          = arbitrary[Document](arbitrarySupportDocument)
          forAll(declarationTypeGen, officeOfDepartureGen, documentGen) {
            (declarationType, officeOfDeparture, document) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationDeclarationTypePage, declarationType)
                .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                .setValue(DocumentSection(Index(0)), Json.obj("foo" -> "bar"))
                .setValue(TypePage(index), document)

              val expectedResult = SupportDocumentDomain(
                document = document
              )

              val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                DocumentDomain.userAnswersReader(index)
              ).run(userAnswers)

              result.value mustBe expectedResult
          }
        }

        "and transport document type" in {
          val declarationTypeGen   = arbitrary[DeclarationType]
          val officeOfDepartureGen = arbitrary[CustomsOffice]
          val documentGen          = arbitrary[Document](arbitraryTransportDocument)
          forAll(declarationTypeGen, officeOfDepartureGen, documentGen) {
            (declarationType, officeOfDeparture, document) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationDeclarationTypePage, declarationType)
                .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                .setValue(DocumentSection(Index(0)), Json.obj("foo" -> "bar"))
                .setValue(TypePage(index), document)

              val expectedResult = TransportDocumentDomain(
                document = document
              )

              val result: EitherType[DocumentDomain] = UserAnswersReader[DocumentDomain](
                DocumentDomain.userAnswersReader(index)
              ).run(userAnswers)

              result.value mustBe expectedResult
          }
        }
      }
    }

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
