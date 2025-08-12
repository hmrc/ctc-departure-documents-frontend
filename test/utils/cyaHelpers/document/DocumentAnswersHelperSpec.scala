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

package utils.cyaHelpers.document

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.document.routes.*
import generators.Generators
import models.reference.Document
import models.{CheckMode, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.*

class DocumentAnswersHelperSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mode: Mode = CheckMode

  "DocumentAnswersHelper" - {

    "attachToAllItems" - {
      "must return None" - {
        "when AttachToAllItems page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.attachToAllItems
          result must not be defined
        }
      }

      "must return Some(Row)" - {
        "when AttachToAllItems page is defined" in {
          val answers = emptyUserAnswers.setValue(AttachToAllItemsPage(documentIndex), true)

          val helper = new DocumentAnswersHelper(answers, documentIndex)
          val result = helper.attachToAllItems.get

          result.key.value mustEqual "Do you want to use this document for all items?"
          result.value.value mustEqual "Yes"

          val actions = result.actions.get.items
          actions.size mustEqual 1
          val action = actions.head
          action.content.value mustEqual "Change"
          action.href mustEqual AttachToAllItemsController.onPageLoad(answers.lrn, mode, documentIndex).url
          action.visuallyHiddenText.get mustEqual "if you want to use this document for all items"
          action.id mustEqual "change-attach-to-all-items"
        }
      }
    }

    "documentType" - {
      "must return None" - {
        "when Type page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.documentType
          result must not be defined
        }
      }

      "must return Some(Row)" - {
        "when Type page is defined" in {
          forAll(arbitrary[Document]) {
            document =>
              val answers = emptyUserAnswers.setValue(TypePage(documentIndex), document)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.documentType.get

              result.key.value mustEqual "Document type"
              result.value.value mustEqual document.toString

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual TypeController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustEqual "document type"
              action.id mustEqual "change-type"
          }
        }
      }
    }

    "previousDocumentType" - {
      "must return None" - {
        "when previousDocumentType page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.previousDocumentType
          result must not be defined
        }
      }

      "must return Some(Row)" - {
        "when previousDocumentType page is defined" in {
          forAll(arbitrary[Document]) {
            document =>
              val answers = emptyUserAnswers.setValue(PreviousDocumentTypePage(documentIndex), document)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.previousDocumentType.get

              result.key.value mustEqual "Document type"
              result.value.value mustEqual document.toString

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual PreviousDocumentTypeController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustEqual "document type"
              action.id mustEqual "change-previous-document-type"
          }
        }
      }
    }

    "documentReferenceNumber" - {
      "must return None" - {
        "when documentReferenceNumber page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.documentReferenceNumber
          result must not be defined
        }
      }

      "must return Some(Row)" - {
        "when documentReferenceNumber page is defined" in {
          forAll(nonEmptyString) {
            reference =>
              val answers = emptyUserAnswers.setValue(DocumentReferenceNumberPage(documentIndex), reference)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.documentReferenceNumber.get

              result.key.value mustEqual "Reference number"
              result.value.value mustEqual reference

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual DocumentReferenceNumberController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustEqual "reference number"
              action.id mustEqual "change-reference-number"
          }
        }
      }
    }

    "lineItemNumberYesNo" - {
      "must return None" - {
        "when AddLineItemNumberYesNo page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.lineItemNumberYesNo
          result must not be defined
        }
      }

      "must return Some(Row)" - {
        "when AddLineItemNumberYesNo page is defined" in {
          val answers = emptyUserAnswers.setValue(AddLineItemNumberYesNoPage(documentIndex), true)

          val helper = new DocumentAnswersHelper(answers, documentIndex)
          val result = helper.lineItemNumberYesNo.get

          result.key.value mustEqual "Do you want to add a line item number to the document?"
          result.value.value mustEqual "Yes"

          val actions = result.actions.get.items
          actions.size mustEqual 1
          val action = actions.head
          action.content.value mustEqual "Change"
          action.href mustEqual AddLineItemNumberYesNoController.onPageLoad(answers.lrn, mode, documentIndex).url
          action.visuallyHiddenText.get mustEqual "if you want to add a line item number to the document"
          action.id mustEqual "change-add-line-item-number"
        }
      }
    }

    "lineItemNumber" - {
      "must return None" - {
        "when LineItemNumberPage is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.lineItemNumber
          result must not be defined
        }
      }

      "must return Some(Row)" - {
        "when LineItemNumberPage page is defined" in {
          forAll(arbitrary[Int]) {
            number =>
              val answers = emptyUserAnswers.setValue(LineItemNumberPage(documentIndex), number)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.lineItemNumber.get

              result.key.value mustEqual "Line item number"
              result.value.value mustEqual number.toString

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual LineItemNumberController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustEqual "line item number"
              action.id mustEqual "change-line-item-number"
          }
        }
      }
    }

    "additionalInformationYesNo" - {
      "must return None" - {
        "when AddAdditionalInformationYesNoPage page is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.additionalInformationYesNo
          result must not be defined
        }
      }

      "must return Some(Row)" - {
        "when AddAdditionalInformationYesNoPage page is defined" in {
          val answers = emptyUserAnswers.setValue(AddAdditionalInformationYesNoPage(documentIndex), true)

          val helper = new DocumentAnswersHelper(answers, documentIndex)
          val result = helper.additionalInformationYesNo.get

          result.key.value mustEqual "Do you want to add any additional information for this document?"
          result.value.value mustEqual "Yes"

          val actions = result.actions.get.items
          actions.size mustEqual 1
          val action = actions.head
          action.content.value mustEqual "Change"
          action.href mustEqual AddAdditionalInformationYesNoController.onPageLoad(answers.lrn, mode, documentIndex).url
          action.visuallyHiddenText.get mustEqual "if you want to add any additional information for this document"
          action.id mustEqual "change-add-additional-information"
        }
      }
    }

    "additionalInformation" - {
      "must return None" - {
        "when AdditionalInformationPage is undefined" in {
          val helper = new DocumentAnswersHelper(emptyUserAnswers, documentIndex)
          val result = helper.additionalInformation
          result must not be defined
        }
      }

      "must return Some(Row)" - {
        "when AdditionalInformationPage page is defined" in {
          forAll(nonEmptyString) {
            additionalInformation =>
              val answers = emptyUserAnswers.setValue(AdditionalInformationPage(documentIndex), additionalInformation)

              val helper = new DocumentAnswersHelper(answers, documentIndex)
              val result = helper.additionalInformation.get

              result.key.value mustEqual "Additional information"
              result.value.value mustEqual additionalInformation

              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual AdditionalInformationController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustEqual "additional information"
              action.id mustEqual "change-additional-information"
          }
        }
      }
    }
  }
}
