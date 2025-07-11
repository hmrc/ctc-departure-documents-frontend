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

import base.SpecBase
import generators.Generators
import models.Index
import models.reference.CustomsOffice
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.AttachToAllItemsPage
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}
import pages.{AddAnotherDocumentPage, AddDocumentsYesNoPage}

class DocumentsDomainSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "Documents" - {

    "can be parsed from UserAnswers" - {

      "when there are documents" in {

        val initialAnswers = emptyUserAnswers.setValue(AddDocumentsYesNoPage, true)

        val numberOfItems = Gen.choose(1, 3).sample.value

        val userAnswers = (0 until numberOfItems).foldLeft(initialAnswers) {
          case (updatedUserAnswers, index) =>
            arbitraryDocumentAnswers(updatedUserAnswers, Index(index)).sample.value
        }

        val result = DocumentsDomain.userAnswersReader.run(userAnswers)

        result.value.value.documents.length mustEqual numberOfItems
        result.value.pages.last mustEqual AddAnotherDocumentPage
      }

      "when there are no documents" - {
        "and Declaration Type is not in set T2 or T2F" in {
          val declarationTypeGen   = arbitrary[String](arbitraryNonT2OrT2FDeclarationType)
          val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
          forAll(declarationTypeGen, officeOfDepartureGen) {
            (declarationType, officeOfDeparture) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationDeclarationTypePage, declarationType)
                .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                .setValue(AddDocumentsYesNoPage, false)

              val result = DocumentsDomain.userAnswersReader.run(userAnswers)

              result.value.value.documents mustEqual Nil
              result.value.pages mustEqual Seq(
                AddDocumentsYesNoPage,
                AddAnotherDocumentPage
              )
          }
        }

        "and Office of Departure is not in GB" in {
          val declarationTypeGen   = arbitrary[String](arbitraryT2OrT2FDeclarationType)
          val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryXiCustomsOffice)
          forAll(declarationTypeGen, officeOfDepartureGen) {
            (declarationType, officeOfDeparture) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationDeclarationTypePage, declarationType)
                .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)
                .setValue(AddDocumentsYesNoPage, false)

              val result = DocumentsDomain.userAnswersReader.run(userAnswers)

              result.value.value.documents mustEqual Nil
              result.value.pages mustEqual Seq(
                AddDocumentsYesNoPage,
                AddAnotherDocumentPage
              )
          }
        }
      }
    }

    "cannot be parsed from user answers" - {

      "when add documents yes/no is unanswered" - {

        "and Declaration Type is not in set T2 or T2F" in {
          val declarationTypeGen   = arbitrary[String](arbitraryNonT2OrT2FDeclarationType)
          val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
          forAll(declarationTypeGen, officeOfDepartureGen) {
            (declarationType, officeOfDeparture) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationDeclarationTypePage, declarationType)
                .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)

              val result = DocumentsDomain.userAnswersReader.run(userAnswers)

              result.left.value.page mustEqual AddDocumentsYesNoPage
              result.left.value.pages mustEqual Seq(
                AddDocumentsYesNoPage
              )
          }
        }

        "and Office of Departure is not in GB" in {
          val declarationTypeGen   = arbitrary[String](arbitraryT2OrT2FDeclarationType)
          val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryXiCustomsOffice)
          forAll(declarationTypeGen, officeOfDepartureGen) {
            (declarationType, officeOfDeparture) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationDeclarationTypePage, declarationType)
                .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)

              val result = DocumentsDomain.userAnswersReader.run(userAnswers)

              result.left.value.page mustEqual AddDocumentsYesNoPage
              result.left.value.pages mustEqual Seq(
                AddDocumentsYesNoPage
              )
          }
        }
      }

      "when Declaration Type is in set T2/T2F and Office of Departure is in GB" - {
        "and attach to all items is unanswered at index 0" in {
          val declarationTypeGen   = arbitrary[String](arbitraryT2OrT2FDeclarationType)
          val officeOfDepartureGen = arbitrary[CustomsOffice](arbitraryGbCustomsOffice)
          forAll(declarationTypeGen, officeOfDepartureGen) {
            (declarationType, officeOfDeparture) =>
              val userAnswers = emptyUserAnswers
                .setValue(TransitOperationDeclarationTypePage, declarationType)
                .setValue(TransitOperationOfficeOfDeparturePage, officeOfDeparture)

              val result = DocumentsDomain.userAnswersReader.run(userAnswers)

              result.left.value.page mustEqual AttachToAllItemsPage(Index(0))
              result.left.value.pages mustEqual Seq(
                AttachToAllItemsPage(Index(0))
              )
          }
        }
      }
    }
  }
}
