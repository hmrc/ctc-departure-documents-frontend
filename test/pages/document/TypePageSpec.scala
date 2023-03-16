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

package pages.document

import models.reference.Document
import pages.behaviours.PageBehaviours
import pages.sections.DocumentDetailsSection
import play.api.libs.json.Json

class TypePageSpec extends PageBehaviours {

  "TypePage" - {

    beRetrievable[Document](TypePage(documentIndex))

    beSettable[Document](TypePage(documentIndex))

    beRemovable[Document](TypePage(documentIndex))

    "cleanup" - {
      val document1 = arbitraryDocument.arbitrary.sample.get
      val document2 = arbitraryDocument.arbitrary.retryUntil(_ != document1).sample.get

      "when answer has changed" - {
        "must clean up document section at document index" in {
          val preChange = emptyUserAnswers
            .setValue(TypePage(documentIndex), document1)
            .setValue(DocumentDetailsSection(documentIndex), Json.obj("foo" -> "bar"))

          val postChange = preChange.setValue(TypePage(documentIndex), document2)

          postChange.get(DocumentDetailsSection(documentIndex)) mustNot be(defined)
        }
      }

      "when answer has not changed" - {
        "must do nothing" in {
          val preChange = emptyUserAnswers
            .setValue(TypePage(documentIndex), document1)
            .setValue(DocumentDetailsSection(documentIndex), Json.obj("foo" -> "bar"))

          val postChange = preChange.setValue(TypePage(documentIndex), document1)

          postChange.get(DocumentDetailsSection(documentIndex)) must be(defined)
        }
      }
    }
  }
}
