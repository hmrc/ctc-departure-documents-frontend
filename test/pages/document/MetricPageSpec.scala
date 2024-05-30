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

import models.reference.Metric
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class MetricPageSpec extends PageBehaviours {

  "MetricPage" - {

    beRetrievable[Metric](MetricPage(documentIndex))

    beSettable[Metric](MetricPage(documentIndex))

    beRemovable[Metric](MetricPage(documentIndex))

    "cleanup" - {
      "when answer changes" - {
        "must remove quantity" in {
          forAll(arbitrary[Metric]) {
            metric =>
              forAll(arbitrary[Metric].retryUntil(_ != metric)) {
                differentMetric =>
                  val userAnswers = emptyUserAnswers
                    .setValue(MetricPage(index), metric)
                    .setValue(QuantityPage(index), arbitrary[BigDecimal].sample.value)

                  val result = userAnswers.setValue(MetricPage(index), differentMetric)

                  result.get(QuantityPage(index)) must not be defined
              }
          }
        }
      }

      "when answer doesn't change" - {
        "must not remove quantity" in {
          forAll(arbitrary[Metric]) {
            metric =>
              val userAnswers = emptyUserAnswers
                .setValue(MetricPage(index), metric)
                .setValue(QuantityPage(index), arbitrary[BigDecimal].sample.value)

              val result = userAnswers.setValue(MetricPage(index), metric)

              result.get(QuantityPage(index)) must be(defined)
          }
        }
      }
    }
  }
}
