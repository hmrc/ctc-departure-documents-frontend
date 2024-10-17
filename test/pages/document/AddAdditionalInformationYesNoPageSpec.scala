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

import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddAdditionalInformationYesNoPageSpec extends PageBehaviours {

  "AddAdditionalInformationYesNoPage" - {

    beRetrievable[Boolean](AddAdditionalInformationYesNoPage(documentIndex))

    beSettable[Boolean](AddAdditionalInformationYesNoPage(documentIndex))

    beRemovable[Boolean](AddAdditionalInformationYesNoPage(documentIndex))

    "cleanup" - {
      "when NO selected" - {
        "must clean up additional information page at document index" in {
          forAll(arbitrary[String]) {
            additionalInformation =>
              val preChange = emptyUserAnswers
                .setValue(AdditionalInformationPage(documentIndex), additionalInformation)

              val postChange = preChange.setValue(AddAdditionalInformationYesNoPage(documentIndex), false)

              postChange.get(AdditionalInformationPage(documentIndex)) mustNot be(defined)
          }
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          forAll(arbitrary[String]) {
            additionalInformation =>
              val preChange = emptyUserAnswers
                .setValue(AdditionalInformationPage(documentIndex), additionalInformation)

              val postChange = preChange.setValue(AddAdditionalInformationYesNoPage(documentIndex), true)

              postChange.get(AdditionalInformationPage(documentIndex)) must be(defined)
          }
        }
      }
    }
  }
}
