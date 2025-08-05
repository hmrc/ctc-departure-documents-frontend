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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.Index
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.AddAnotherDocumentViewModel.AddAnotherDocumentViewModelProvider

class AddAnotherDocumentViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one document" in {
      val userAnswers = arbitraryDocumentAnswers(emptyUserAnswers, index).sample.value

      val result = new AddAnotherDocumentViewModelProvider()(userAnswers)
      result.count mustEqual 1
      result.title mustEqual "You have added 1 document"
      result.heading mustEqual "You have added 1 document"
      result.legend mustEqual "Do you want to add another document?"
      result.maxLimitLabel mustEqual "You cannot attach any more documents to all items. You can still add documents and attach them to individual items."
    }

    "when there are multiple documents" in {
      val formatter = java.text.NumberFormat.getIntegerInstance

      forAll(Gen.choose(2, 3)) {
        numberOfDocuments =>
          val userAnswers = (0 until numberOfDocuments).foldLeft(emptyUserAnswers) {
            (acc, i) =>
              arbitraryDocumentAnswers(acc, Index(i)).sample.value
          }

          val result = new AddAnotherDocumentViewModelProvider()(userAnswers)
          result.count mustEqual numberOfDocuments
          result.title mustEqual s"You have added ${formatter.format(numberOfDocuments)} documents"
          result.heading mustEqual s"You have added ${formatter.format(numberOfDocuments)} documents"
          result.legend mustEqual "Do you want to add another document?"
          result.maxLimitLabel mustEqual "You cannot attach any more documents to all items. You can still add documents and attach them to individual items."
      }
    }
  }
}
