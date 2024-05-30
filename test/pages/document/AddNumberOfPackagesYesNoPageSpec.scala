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

class AddNumberOfPackagesYesNoPageSpec extends PageBehaviours {

  "DeclarePackageGoodsYesNoPage" - {

    beRetrievable[Boolean](AddNumberOfPackagesYesNoPage(documentIndex))

    beSettable[Boolean](AddNumberOfPackagesYesNoPage(documentIndex))

    beRemovable[Boolean](AddNumberOfPackagesYesNoPage(documentIndex))

    "cleanup" - {
      "when no selected" - {
        "must remove number of packages" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddNumberOfPackagesYesNoPage(index), true)
            .setValue(NumberOfPackagesPage(index), arbitrary[Int].sample.value)

          val result = userAnswers.setValue(AddNumberOfPackagesYesNoPage(index), false)

          result.get(NumberOfPackagesPage(index)) must not be defined
        }
      }
    }
  }
}
