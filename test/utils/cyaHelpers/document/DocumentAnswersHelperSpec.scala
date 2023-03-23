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
import models.Mode
import models.reference.{Document, PackageType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._

class DocumentAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "DocumentAnswersHelper" - {

    "documentType" - {
      "must return None" - {
        "when Type page is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.documentType
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when Type page is defined" in {
          forAll(arbitrary[Mode], arbitrary[Document]) {
            (mode, document) =>
              val answers = emptyUserAnswers.setValue(TypePage(documentIndex), document)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
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
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.previousDocumentType
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when previousDocumentType page is defined" in {
          forAll(arbitrary[Mode], arbitrary[Document]) {
            (mode, document) =>
              val answers = emptyUserAnswers.setValue(PreviousDocumentTypePage(documentIndex), document)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
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
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.documentReferenceNumber
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when documentReferenceNumber page is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, reference) =>
              val answers = emptyUserAnswers.setValue(DocumentReferenceNumberPage(documentIndex), reference)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
              val result = helper.documentReferenceNumber.get

              result.key.value mustBe "Document reference number"
              result.value.value mustBe reference

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe DocumentReferenceNumberController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "document reference number"
              action.id mustBe "change-document-reference-number"
          }
        }
      }
    }

    "goodsItemNumberYesNo" - {
      "must return None" - {
        "when AddGoodsItemNumberYesNo page is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.goodsItemNumberYesNo
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when AddGoodsItemNumberYesNo page is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddGoodsItemNumberYesNoPage(documentIndex), true)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
              val result = helper.goodsItemNumberYesNo.get

              result.key.value mustBe "Do you want to add a goods item number?"
              result.value.value mustBe "Yes"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe AddGoodsItemNumberYesNoController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "if you want to add a goods item number"
              action.id mustBe "change-add-goods-item-number"
          }
        }
      }
    }

    "goodsItemNumber" - {
      "must return None" - {
        "when GoodsItemNumber page is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.goodsItemNumber
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when GoodsItemNumber page is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, number) =>
              val answers = emptyUserAnswers.setValue(GoodsItemNumberPage(documentIndex), number)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
              val result = helper.goodsItemNumber.get

              result.key.value mustBe "Reference number"
              result.value.value mustBe number

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe GoodsItemNumberController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "reference number"
              action.id mustBe "change-reference-number"
          }
        }
      }
    }

    "typeOfPackageYesNo" - {
      "must return None" - {
        "when AddTypeOfPackageYesNo page is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.typeOfPackageYesNo
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when AddTypeOfPackageYesNo page is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddTypeOfPackageYesNoPage(documentIndex), true)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
              val result = helper.typeOfPackageYesNo.get

              result.key.value mustBe "Do you want to declare the package the goods arrived in?"
              result.value.value mustBe "Yes"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe AddTypeOfPackageYesNoController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "if you want to declare the package the goods arrived in"
              action.id mustBe "change-add-type-of-package"
          }
        }
      }
    }

    "packageType" - {
      "must return None" - {
        "when PackageType page is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.packageType
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when PackageType page is defined" in {
          forAll(arbitrary[Mode], arbitrary[PackageType]) {
            (mode, packageType) =>
              val answers = emptyUserAnswers.setValue(PackageTypePage(documentIndex), packageType)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
              val result = helper.packageType.get

              result.key.value mustBe "Package type"
              result.value.value mustBe packageType.toString

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe PackageTypeController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "package type"
              action.id mustBe "change-package-type"
          }
        }
      }
    }

    "lineItemNumberYesNo" - {
      "must return None" - {
        "when AddLineItemNumberYesNo page is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.lineItemNumberYesNo
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when AddLineItemNumberYesNo page is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddLineItemNumberYesNoPage(documentIndex), true)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
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
    }

    "lineItemNumber" - {
      "must return None" - {
        "when LineItemNumberPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.lineItemNumber
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when LineItemNumberPage page is defined" in {
          forAll(arbitrary[Mode], arbitrary[Int]) {
            (mode, number) =>
              val answers = emptyUserAnswers.setValue(LineItemNumberPage(documentIndex), number)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
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

    "declareQuantityOfGoodsYesNo" - {
      "must return None" - {
        "when DeclareQuantityOfGoodsYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.declareQuantityOfGoodsYesNo
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when DeclareQuantityOfGoodsYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(DeclareQuantityOfGoodsYesNoPage(documentIndex), true)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
              val result = helper.declareQuantityOfGoodsYesNo.get

              result.key.value mustBe "Do you want to declare the quantity of goods?"
              result.value.value mustBe "Yes"

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe DeclareQuantityOfGoodsYesNoController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "if you want to declare the quantity of goods"
              action.id mustBe "change-declare-quantity-of-goods"
          }
        }
      }
    }

    "numberOfPackage" - {
      "must return None" - {
        "when NumberOfPackagesPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DocumentAnswersHelper(emptyUserAnswers, mode, documentIndex)
              val result = helper.numberOfPackage
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when NumberOfPackagesPage page is defined" in {
          forAll(arbitrary[Mode], arbitrary[Int]) {
            (mode, number) =>
              val answers = emptyUserAnswers.setValue(NumberOfPackagesPage(documentIndex), number)

              val helper = new DocumentAnswersHelper(answers, mode, documentIndex)
              val result = helper.numberOfPackage.get

              result.key.value mustBe "Number of packages"
              result.value.value mustBe number.toString

              val actions = result.actions.get.items
              actions.size mustBe 1
              val action = actions.head
              action.content.value mustBe "Change"
              action.href mustBe NumberOfPackagesController.onPageLoad(answers.lrn, mode, documentIndex).url
              action.visuallyHiddenText.get mustBe "number of packages"
              action.id mustBe "change-number-of-packages"
          }
        }
      }
    }

  }
}
