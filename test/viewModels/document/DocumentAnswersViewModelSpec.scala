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
import models.Mode
import models.reference.{Document, PackageType}
import pages.document._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.document.DocumentAnswersViewModel.DocumentAnswersViewModelProvider

class DocumentAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val viewModelProvider = app.injector.instanceOf[DocumentAnswersViewModelProvider]

  "when transport document" - {
    "must render 2 rows" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val userAnswers = emptyUserAnswers
            .setValue(TypePage(index), arbitrary[Document](arbitraryTransportDocument).sample.value)
            .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)

          val result = viewModelProvider.apply(userAnswers, mode, index).sections.head

          result.sectionTitle must not be defined

          result.rows.size mustBe 2

          result.addAnotherLink must not be defined
      }
    }
  }

  "when support document" - {
    "must render 4 rows" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val userAnswers = emptyUserAnswers
            .setValue(TypePage(index), arbitrary[Document](arbitrarySupportDocument).sample.value)
            .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)
          // TODO - include line item number pages

          val result = viewModelProvider.apply(userAnswers, mode, index).sections.head

          result.sectionTitle must not be defined

          result.rows.size mustBe 2

          result.addAnotherLink must not be defined
      }
    }
  }

  "when previous document" - {
    "must render 11 rows" in {
      forAll(arbitrary[Mode], Gen.oneOf(TypePage, PreviousDocumentTypePage)) {
        (mode, typePage) =>
          val userAnswers = emptyUserAnswers
            .setValue(typePage(index), arbitrary[Document](arbitraryPreviousDocument).sample.value)
            .setValue(DocumentReferenceNumberPage(index), nonEmptyString.sample.value)
            .setValue(AddGoodsItemNumberYesNoPage(index), true)
            .setValue(GoodsItemNumberPage(index), nonEmptyString.sample.value)
            .setValue(AddTypeOfPackageYesNoPage(index), true)
            .setValue(PackageTypePage(index), arbitrary[PackageType].sample.value)
          // TODO - include remaining pages

          val result = viewModelProvider.apply(userAnswers, mode, index).sections.head

          result.sectionTitle must not be defined

          result.rows.size mustBe 6

          result.addAnotherLink must not be defined
      }
    }
  }
}
