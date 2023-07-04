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

import models.reference.{Document, Metric, PackageType}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AttachToAllItemsPageSpec extends PageBehaviours {

  "AttachToAllItemsPage" - {

    beRetrievable[Boolean](AttachToAllItemsPage(documentIndex))

    beSettable[Boolean](AttachToAllItemsPage(documentIndex))

    beRemovable[Boolean](AttachToAllItemsPage(documentIndex))

    "cleanup" - {

      "when answer changes" - {
        "must remove pages" in {
          forAll(arbitrary[Boolean]) {
            bool =>
              val userAnswers = emptyUserAnswers
                .setValue(AttachToAllItemsPage(index), bool)
                .setValue(InferredAttachToAllItemsPage(index), false)
                .setValue(TypePage(index), arbitrary[Document].sample.value)
                .setValue(PreviousDocumentTypePage(index), arbitrary[Document].sample.value)
                .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)
                .setValue(AddTypeOfPackageYesNoPage(index), true)
                .setValue(PackageTypePage(index), arbitrary[PackageType].sample.value)
                .setValue(AddNumberOfPackagesYesNoPage(index), true)
                .setValue(NumberOfPackagesPage(index), arbitrary[Int].sample.value)
                .setValue(DeclareQuantityOfGoodsYesNoPage(index), true)
                .setValue(MetricPage(index), arbitrary[Metric].sample.value)
                .setValue(QuantityPage(index), arbitrary[BigDecimal].sample.value)
                .setValue(AddAdditionalInformationYesNoPage(index), true)
                .setValue(AdditionalInformationPage(index), nonEmptyString.sample.value)

              val result = userAnswers.setValue(AttachToAllItemsPage(index), !bool)

              result.get(InferredAttachToAllItemsPage(index)) must not be defined
              result.get(TypePage(index)) must not be defined
              result.get(PreviousDocumentTypePage(index)) must not be defined
              result.get(DocumentReferenceNumberPage(index)) must not be defined
              result.get(AddTypeOfPackageYesNoPage(index)) must not be defined
              result.get(PackageTypePage(index)) must not be defined
              result.get(AddNumberOfPackagesYesNoPage(index)) must not be defined
              result.get(NumberOfPackagesPage(index)) must not be defined
              result.get(DeclareQuantityOfGoodsYesNoPage(index)) must not be defined
              result.get(MetricPage(index)) must not be defined
              result.get(QuantityPage(index)) must not be defined
              result.get(AddAdditionalInformationYesNoPage(index)) must not be defined
              result.get(AdditionalInformationPage(index)) must not be defined
          }
        }
      }

      "when answer does not change" - {
        "must not remove pages" in {
          forAll(arbitrary[Boolean]) {
            bool =>
              val userAnswers = emptyUserAnswers
                .setValue(AttachToAllItemsPage(index), bool)
                .setValue(TypePage(index), arbitrary[Document].sample.value)
                .setValue(PreviousDocumentTypePage(index), arbitrary[Document].sample.value)
                .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)
                .setValue(AddTypeOfPackageYesNoPage(index), true)
                .setValue(PackageTypePage(index), arbitrary[PackageType].sample.value)
                .setValue(AddNumberOfPackagesYesNoPage(index), true)
                .setValue(NumberOfPackagesPage(index), arbitrary[Int].sample.value)
                .setValue(DeclareQuantityOfGoodsYesNoPage(index), true)
                .setValue(MetricPage(index), arbitrary[Metric].sample.value)
                .setValue(QuantityPage(index), arbitrary[BigDecimal].sample.value)
                .setValue(AddAdditionalInformationYesNoPage(index), true)
                .setValue(AdditionalInformationPage(index), nonEmptyString.sample.value)

              val result = userAnswers.setValue(AttachToAllItemsPage(index), bool)

              result.get(TypePage(index)) must be(defined)
              result.get(PreviousDocumentTypePage(index)) must be(defined)
              result.get(DocumentReferenceNumberPage(index)) must be(defined)
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
}
