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

package viewModels.document

import base.SpecBase
import generators.Generators
import models.reference.{Document, Metric, PackageType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._
import viewModels.document.DocumentAnswersViewModel.DocumentAnswersViewModelProvider

class DocumentAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val viewModelProvider = app.injector.instanceOf[DocumentAnswersViewModelProvider]

  "when transport document" - {
    "must render 3 rows" in {
      val userAnswers = emptyUserAnswers
        .setValue(TypePage(index), arbitrary[Document](arbitraryTransportDocument).sample.value)
        .setValue(AttachToAllItemsPage(index), arbitrary[Boolean].sample.value)
        .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)

      val result = viewModelProvider.apply(userAnswers, index).sections.head

      result.sectionTitle must not be defined

      result.rows.size mustBe 3

      result.addAnotherLink must not be defined
    }
  }

  "when support document" - {
    "must render 7 rows" in {
      val userAnswers = emptyUserAnswers
        .setValue(TypePage(index), arbitrary[Document](arbitrarySupportDocument).sample.value)
        .setValue(AttachToAllItemsPage(index), arbitrary[Boolean].sample.value)
        .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)
        .setValue(AddLineItemNumberYesNoPage(index), true)
        .setValue(LineItemNumberPage(index), positiveIntsMinMax(0, 99999).sample.value)
        .setValue(AddAdditionalInformationYesNoPage(index), true)
        .setValue(AdditionalInformationPage(index), nonEmptyString.sample.value)

      val result = viewModelProvider.apply(userAnswers, index).sections.head

      result.sectionTitle must not be defined

      result.rows.size mustBe 7

      result.addAnotherLink must not be defined
    }
  }

  "when previous document" - {
    "must render 14 rows" in {
      forAll(Gen.oneOf(TypePage, PreviousDocumentTypePage)) {
        typePage =>
          val userAnswers = emptyUserAnswers
            .setValue(typePage(index), arbitrary[Document](arbitraryPreviousDocument).sample.value)
            .setValue(AttachToAllItemsPage(index), arbitrary[Boolean].sample.value)
            .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)
            .setValue(AddGoodsItemNumberYesNoPage(index), true)
            .setValue(GoodsItemNumberPage(index), arbitrary[Int].sample.value)
            .setValue(AddTypeOfPackageYesNoPage(index), true)
            .setValue(PackageTypePage(index), arbitrary[PackageType].sample.value)
            .setValue(AddNumberOfPackagesYesNoPage(index), true)
            .setValue(NumberOfPackagesPage(index), positiveIntsMinMax(0, 99999999).sample.value)
            .setValue(DeclareQuantityOfGoodsYesNoPage(index), true)
            .setValue(MetricPage(index), arbitrary[Metric].sample.value)
            .setValue(QuantityPage(index), arbitrary[BigDecimal].sample.value)
            .setValue(AddAdditionalInformationYesNoPage(index), true)
            .setValue(AdditionalInformationPage(index), nonEmptyString.sample.value)

          val result = viewModelProvider.apply(userAnswers, index).sections.head

          result.sectionTitle must not be defined

          result.rows.size mustBe 14

          result.addAnotherLink must not be defined
      }
    }
  }
}
