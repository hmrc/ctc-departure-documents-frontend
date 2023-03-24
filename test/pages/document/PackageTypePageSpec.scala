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

class PackageTypePageSpec extends PageBehaviours {

  "PackageTypePage" - {

    beRetrievable[PackageType](PackageTypePage(documentIndex))

    beSettable[PackageType](PackageTypePage(documentIndex))

    beRemovable[PackageType](PackageTypePage(documentIndex))

    "cleanup" - {
      "when answer changes" - {
        "must remove number of packages pages" in {
          forAll(arbitrary[PackageType]) {
            packageType =>
              forAll(arbitrary[PackageType].retryUntil(_ != packageType)) {
                differentPackageType =>
                  val userAnswers = emptyUserAnswers
                    .setValue(PackageTypePage(index), packageType)
                    .setValue(AddNumberOfPackagesYesNoPage(index), true)
                    .setValue(NumberOfPackagesPage(index), arbitrary[Int].sample.value)

                  val result = userAnswers.setValue(PackageTypePage(index), differentPackageType)

                  result.get(AddNumberOfPackagesYesNoPage(index)) must not be defined
                  result.get(NumberOfPackagesPage(index)) must not be defined
              }
          }
        }
      }

      "when answer doesn't change" - {
        "must do nothing" in {
          forAll(arbitrary[PackageType]) {
            packageType =>
              val userAnswers = emptyUserAnswers
                .setValue(PackageTypePage(index), packageType)
                .setValue(AddNumberOfPackagesYesNoPage(index), true)
                .setValue(NumberOfPackagesPage(index), arbitrary[Int].sample.value)

              val result = userAnswers.setValue(PackageTypePage(index), packageType)

              result.get(AddNumberOfPackagesYesNoPage(index)) must be(defined)
              result.get(NumberOfPackagesPage(index)) must be(defined)
          }
        }
      }
    }
  }
}
