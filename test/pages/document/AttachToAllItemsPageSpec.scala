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

import models.reference.{Metric, PackageType}
import pages.behaviours.PageBehaviours
import org.scalacheck.Arbitrary.arbitrary

class AttachToAllItemsPageSpec extends PageBehaviours {

  "AttachToAllItemsPage" - {

    beRetrievable[Boolean](AttachToAllItemsPage(documentIndex))

    beSettable[Boolean](AttachToAllItemsPage(documentIndex))

    beRemovable[Boolean](AttachToAllItemsPage(documentIndex))

    "cleanup" - {

      "when YES selected" - {
        "must remove pages" in {
          val userAnswers = emptyUserAnswers
            .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)
            .setValue(AddGoodsItemNumberYesNoPage(index), true)
            .setValue(GoodsItemNumberPage(index), arbitrary[Int].sample.value)
            .setValue(AddTypeOfPackageYesNoPage(index), true)
            .setValue(PackageTypePage(index), arbitrary[PackageType].sample.value)
            .setValue(AddNumberOfPackagesYesNoPage(index), true)
            .setValue(NumberOfPackagesPage(index), arbitrary[Int].sample.value)
            .setValue(DeclareQuantityOfGoodsYesNoPage(index), true)
            .setValue(MetricPage(index), arbitrary[Metric].sample.value)
            .setValue(QuantityPage(index), arbitrary[BigDecimal].sample.value)
            .setValue(AddAdditionalInformationYesNoPage(index), true)
            .setValue(AdditionalInformationPage(index), nonEmptyString.sample.value)

          val result = userAnswers.setValue(AttachToAllItemsPage(index), true)

          result.get(DocumentReferenceNumberPage(index)) must be(defined)

          result.get(AddGoodsItemNumberYesNoPage(index)) must not be defined
          result.get(GoodsItemNumberPage(index)) must not be defined
          result.get(AddTypeOfPackageYesNoPage(index)) must not be defined
          result.get(PackageTypePage(index)) must not be defined
          result.get(AddNumberOfPackagesYesNoPage(index)) must not be defined
          result.get(NumberOfPackagesPage(index)) must not be defined
          result.get(DeclareQuantityOfGoodsYesNoPage(index)) must not be defined
          result.get(MetricPage(index)) must not be defined
          result.get(QuantityPage(index)) must not be defined

          result.get(AddAdditionalInformationYesNoPage(index)) must be(defined)
          result.get(AdditionalInformationPage(index)) must be(defined)
        }
      }

      "when NO selected" - {
        "must not remove pages" in {
          val userAnswers = emptyUserAnswers
            .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)
            .setValue(AddGoodsItemNumberYesNoPage(index), true)
            .setValue(GoodsItemNumberPage(index), arbitrary[Int].sample.value)
            .setValue(AddTypeOfPackageYesNoPage(index), true)
            .setValue(PackageTypePage(index), arbitrary[PackageType].sample.value)
            .setValue(AddNumberOfPackagesYesNoPage(index), true)
            .setValue(NumberOfPackagesPage(index), arbitrary[Int].sample.value)
            .setValue(DeclareQuantityOfGoodsYesNoPage(index), true)
            .setValue(MetricPage(index), arbitrary[Metric].sample.value)
            .setValue(QuantityPage(index), arbitrary[BigDecimal].sample.value)
            .setValue(AddAdditionalInformationYesNoPage(index), true)
            .setValue(AdditionalInformationPage(index), nonEmptyString.sample.value)

          val result = userAnswers.setValue(AttachToAllItemsPage(index), false)

          result.get(DocumentReferenceNumberPage(index)) must be(defined)

          result.get(AddGoodsItemNumberYesNoPage(index)) must be(defined)
          result.get(GoodsItemNumberPage(index)) must be(defined)
          result.get(AddTypeOfPackageYesNoPage(index)) must be(defined)
          result.get(PackageTypePage(index)) must be(defined)
          result.get(AddNumberOfPackagesYesNoPage(index)) must be(defined)
          result.get(NumberOfPackagesPage(index)) must be(defined)
          result.get(DeclareQuantityOfGoodsYesNoPage(index)) must be(defined)
          result.get(MetricPage(index)) must be(defined)
          result.get(QuantityPage(index)) must be(defined)

          result.get(AddAdditionalInformationYesNoPage(index)) must be(defined)
          result.get(AdditionalInformationPage(index)) must be(defined)
        }
      }
    }
  }
}
