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
        "must clean up package type page at document index" in {
          forAll(arbitrary[PackageType]) {
            packageType =>
              val preChange = emptyUserAnswers
                .setValue(PackageTypePage(documentIndex), packageType)

              val postChange = preChange.setValue(AddTypeOfPackageYesNoPage(documentIndex), false)

              postChange.get(PackageTypePage(documentIndex)) mustNot be(defined)
          }
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          forAll(arbitrary[PackageType]) {
            packageType =>
              val preChange = emptyUserAnswers
                .setValue(PackageTypePage(documentIndex), packageType)

              val postChange = preChange.setValue(AddTypeOfPackageYesNoPage(documentIndex), true)

              postChange.get(PackageTypePage(documentIndex)) must be(defined)
          }
        }
      }
    }
  }
}
