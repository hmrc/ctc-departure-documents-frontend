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
import controllers.document.routes._
import generators.Generators
import models.reference.Document
import models.{CheckMode, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._

class DocumentAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mode: Mode = CheckMode

  "DocumentAnswersHelper" - {

    "attachToAllItems" - {
      "must return None" - {
        "when AttachToAllItems page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.attachToAllItems
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        "when AttachToAllItems page is defined" in {
          val answers = emptyUserAnswers.setValue(AttachToAllItemsPage(documentIndex), true)

          val helper = new DocumentAnswersHelper(answers, documentIndex)
          val result = helper.attachToAllItems.get

          result.key.value mustBe "Do you want to use this document for all items?"
          result.value.value mustBe "Yes"

          val actions = result.actions.get.items
          actions.size mustBe 1
          val action = actions.head
          action.content.value mustBe "Change"
          action.href mustBe AttachToAllItemsController.onPageLoad(answers.lrn, mode, documentIndex).url
          action.visuallyHiddenText.get mustBe "if you want to use this document for all items"
          action.id mustBe "change-attach-to-all-items"
        }
      }
    }

    "documentType" - {
      "must return None" - {
        "when Type page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.documentType
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        "when Type page is defined" in {
          forAll(arbitrary[Document]) {
            document =>
              val answers = emptyUserAnswers.setValue(TypePage(documentIndex), document)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.documentType.get

              result.key.value mustBe "Document type"
              result.value.value mustBe document.toString

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe TypeController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "document type"
              action.id mustBe "change-type"
          }
        }
      }
    }

    "previousDocumentType" - {
      "must return None" - {
        "when previousDocumentType page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.previousDocumentType
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        "when previousDocumentType page is defined" in {
          forAll(arbitrary[Document]) {
            document =>
              val answers = emptyUserAnswers.setValue(PreviousDocumentTypePage(documentIndex), document)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.previousDocumentType.get

              result.key.value mustBe "Document type"
              result.value.value mustBe document.toString

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe PreviousDocumentTypeController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "document type"
              action.id mustBe "change-previous-document-type"
          }
        }
      }
    }

    "documentReferenceNumber" - {
      "must return None" - {
        "when documentReferenceNumber page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.documentReferenceNumber
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        "when documentReferenceNumber page is defined" in {
          forAll(nonEmptyString) {
            reference =>
              val answers = emptyUserAnswers.setValue(DocumentReferenceNumberPage(documentIndex), reference)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.documentReferenceNumber.get

              result.key.value mustBe "Reference number"
              result.value.value mustBe reference

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe DocumentReferenceNumberController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "reference number"
              action.id mustBe "change-reference-number"
          }
        }
      }
    }

    "lineItemNumberYesNo" - {
      "must return None" - {
        "when AddLineItemNumberYesNo page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.lineItemNumberYesNo
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        "when AddLineItemNumberYesNo page is defined" in {
          val answers = emptyUserAnswers.setValue(AddLineItemNumberYesNoPage(documentIndex), true)

          val helper = new DocumentAnswersHelper(answers, documentIndex)
          val result = helper.lineItemNumberYesNo.get

          result.key.value mustBe "Do you want to add a line item number to the document?"
          result.value.value mustBe "Yes"

          val actions = result.actions.get.items
          actions.size mustBe 1
          val action = actions.head
          action.content.value mustBe "Change"
          action.href mustBe AddLineItemNumberYesNoController.onPageLoad(answers.lrn, mode, documentIndex).url
          action.visuallyHiddenText.get mustBe "if you want to add a line item number to the document"
          action.id mustBe "change-add-line-item-number"
        }
      }
    }

    "lineItemNumber" - {
      "must return None" - {
        "when LineItemNumberPage is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.lineItemNumber
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        "when LineItemNumberPage page is defined" in {
          forAll(arbitrary[Int]) {
            number =>
              val answers = emptyUserAnswers.setValue(LineItemNumberPage(documentIndex), number)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.lineItemNumber.get

              result.key.value mustBe "Line item number"
              result.value.value mustBe number.toString

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe LineItemNumberController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "line item number"
              action.id mustBe "change-line-item-number"
          }
        }
      }
    }

  }
}
