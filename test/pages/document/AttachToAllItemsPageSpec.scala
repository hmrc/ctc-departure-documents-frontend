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

package pages.document

import models.reference.Document
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.sections.DocumentDetailsSection
import play.api.libs.json.Json

class AttachToAllItemsPageSpec extends PageBehaviours {

  "AttachToAllItemsPage" - {

    beRetrievable[Boolean](AttachToAllItemsPage(documentIndex))

    beSettable[Boolean](AttachToAllItemsPage(documentIndex))

    beRemovable[Boolean](AttachToAllItemsPage(documentIndex))

    "cleanup" - {
      "must remove pages" in {
        forAll(arbitrary[Boolean]) {
          bool =>
            val userAnswers = emptyUserAnswers
              .setValue(InferredAttachToAllItemsPage(index), bool)
              .setValue(TypePage(index), arbitrary[Document].sample.value)
              .setValue(PreviousDocumentTypePage(index), arbitrary[Document].sample.value)
              .setValue(DocumentDetailsSection(index), Json.obj("foo" -> "bar"))

            val result = userAnswers.setValue(AttachToAllItemsPage(index), bool)

            result.get(AttachToAllItemsPage(index)) mustBe defined
            result.get(InferredAttachToAllItemsPage(index)) must not be defined
            result.get(TypePage(index)) must not be defined
            result.get(PreviousDocumentTypePage(index)) must not be defined
            result.get(DocumentDetailsSection(index)) must not be defined
        }
      }
    }
  }
}

class InferredAttachToAllItemsPageSpec extends PageBehaviours {

  "InferredAttachToAllItemsPage" - {

    beRetrievable[Boolean](InferredAttachToAllItemsPage(documentIndex))

    beSettable[Boolean](InferredAttachToAllItemsPage(documentIndex))

    beRemovable[Boolean](InferredAttachToAllItemsPage(documentIndex))

    "cleanup" - {
      "must remove pages" in {
        forAll(arbitrary[Boolean]) {
          bool =>
            val userAnswers = emptyUserAnswers
              .setValue(AttachToAllItemsPage(index), bool)
              .setValue(TypePage(index), arbitrary[Document].sample.value)
              .setValue(PreviousDocumentTypePage(index), arbitrary[Document].sample.value)
              .setValue(DocumentDetailsSection(index), Json.obj("foo" -> "bar"))

            val result = userAnswers.setValue(InferredAttachToAllItemsPage(index), bool)

            result.get(InferredAttachToAllItemsPage(index)) mustBe defined
            result.get(AttachToAllItemsPage(index)) must not be defined
            result.get(TypePage(index)) must not be defined
            result.get(PreviousDocumentTypePage(index)) must not be defined
            result.get(DocumentDetailsSection(index)) must not be defined
        }
      }
    }
  }
}
