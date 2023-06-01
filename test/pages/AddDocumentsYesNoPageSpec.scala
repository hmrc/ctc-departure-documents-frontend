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

package pages

import pages.behaviours.PageBehaviours
import pages.sections.DocumentsSection
import play.api.libs.json.Json

class AddDocumentsYesNoPageSpec extends PageBehaviours {

  "AddDocumentsYesNoPage" - {

    beRetrievable[Boolean](AddDocumentsYesNoPage)

    beSettable[Boolean](AddDocumentsYesNoPage)

    beRemovable[Boolean](AddDocumentsYesNoPage)

    "cleanup" - {
      "when NO selected" - {
        "must clean up documents section" in {
          val preChange = emptyUserAnswers.setValue(DocumentsSection, Json.arr("foo" -> "bar"))

          val postChange = preChange.setValue(AddDocumentsYesNoPage, false)

          postChange.get(DocumentsSection) mustNot be(defined)
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          val preChange = emptyUserAnswers.setValue(DocumentsSection, Json.arr("foo" -> "bar"))

          val postChange = preChange.setValue(AddDocumentsYesNoPage, true)

          postChange.get(DocumentsSection) must be(defined)
        }
      }
    }
  }
}
