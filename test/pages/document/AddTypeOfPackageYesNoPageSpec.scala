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

import models.reference.PackageType
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddTypeOfPackageYesNoPageSpec extends PageBehaviours {

  "AddTypeOfPackageYesNoPage" - {

    beRetrievable[Boolean](AddTypeOfPackageYesNoPage(documentIndex))

    beSettable[Boolean](AddTypeOfPackageYesNoPage(documentIndex))

    beRemovable[Boolean](AddTypeOfPackageYesNoPage(documentIndex))

    "cleanup" - {
      "when NO selected" - {
        "must clean up package pages at document index" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddTypeOfPackageYesNoPage(documentIndex), true)
            .setValue(PackageTypePage(documentIndex), arbitrary[PackageType].sample.value)
            .setValue(AddNumberOfPackagesYesNoPage(documentIndex), true)
            .setValue(NumberOfPackagesPage(documentIndex), arbitrary[Int].sample.value)

          val result = userAnswers.setValue(AddTypeOfPackageYesNoPage(documentIndex), false)

          result.get(PackageTypePage(documentIndex)) must not be defined
          result.get(AddNumberOfPackagesYesNoPage(documentIndex)) must not be defined
          result.get(NumberOfPackagesPage(documentIndex)) must not be defined
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddTypeOfPackageYesNoPage(documentIndex), true)
            .setValue(PackageTypePage(documentIndex), arbitrary[PackageType].sample.value)
            .setValue(AddNumberOfPackagesYesNoPage(documentIndex), true)
            .setValue(NumberOfPackagesPage(documentIndex), arbitrary[Int].sample.value)

          val result = userAnswers.setValue(AddTypeOfPackageYesNoPage(documentIndex), true)

          result.get(PackageTypePage(documentIndex)) must be(defined)
          result.get(AddNumberOfPackagesYesNoPage(documentIndex)) must be(defined)
          result.get(NumberOfPackagesPage(documentIndex)) must be(defined)
        }
      }
    }
  }
}
