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

package models.journeyDomain

import base.SpecBase
import generators.Generators
import models.reference.PackageType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._

class PackageDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "can be read from user answers" - {
    "when package type is answered" in {
      forAll(arbitrary[PackageType], arbitrary[Int]) {
        (packageType, numberOfPackages) =>
          val userAnswers = emptyUserAnswers
            .setValue(PackageTypePage(index), packageType)
            .setValue(AddNumberOfPackagesYesNoPage(index), true)
            .setValue(NumberOfPackagesPage(index), numberOfPackages)

          val expectedResult = PackageDomain(
            `type` = packageType,
            numberOfPackages = Some(numberOfPackages)
          )

          val result = PackageDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            PackageTypePage(index),
            AddNumberOfPackagesYesNoPage(index),
            NumberOfPackagesPage(index)
          )
      }
    }
  }

  "can not be read from user answers" - {
    "when package type is unanswered" in {
      val result = PackageDomain.userAnswersReader(index).apply(Nil).run(emptyUserAnswers)

      result.left.value.page mustBe PackageTypePage(index)
      result.left.value.pages mustBe Seq(
        PackageTypePage(index)
      )
    }

    "when add number of packages yes/no is unanswered" in {
      forAll(arbitrary[PackageType]) {
        packageType =>
          val userAnswers = emptyUserAnswers
            .setValue(PackageTypePage(index), packageType)

          val result = PackageDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.left.value.page mustBe AddNumberOfPackagesYesNoPage(index)
          result.left.value.pages mustBe Seq(
            PackageTypePage(index),
            AddNumberOfPackagesYesNoPage(index)
          )
      }
    }

    "when number of packages is unanswered" in {
      forAll(arbitrary[PackageType]) {
        packageType =>
          val userAnswers = emptyUserAnswers
            .setValue(PackageTypePage(index), packageType)
            .setValue(AddNumberOfPackagesYesNoPage(index), true)

          val result = PackageDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.left.value.page mustBe NumberOfPackagesPage(index)
          result.left.value.pages mustBe Seq(
            PackageTypePage(index),
            AddNumberOfPackagesYesNoPage(index),
            NumberOfPackagesPage(index)
          )
      }
    }
  }
}
