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
import models.reference.Document
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document.TypePage

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
  }
}
