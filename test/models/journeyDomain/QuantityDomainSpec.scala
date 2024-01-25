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
import models.reference.Metric
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.document._

class QuantityDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "can be read from user answers" - {
    "when both questions answered" in {
      forAll(arbitrary[Metric], arbitrary[BigDecimal]) {
        (metric, value) =>
          val userAnswers = emptyUserAnswers
            .setValue(MetricPage(index), metric)
            .setValue(QuantityPage(index), value)

          val expectedResult = QuantityDomain(
            metric = metric,
            value = value
          )

          val result = QuantityDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            MetricPage(index),
            QuantityPage(index)
          )
      }
    }
  }

  "can not be read from user answers" - {
    "when metric is unanswered" in {
      val result = QuantityDomain.userAnswersReader(index).apply(Nil).run(emptyUserAnswers)

      result.left.value.page mustBe MetricPage(index)
      result.left.value.pages mustBe Seq(
        MetricPage(index)
      )
    }

    "when quantity is unanswered" in {
      forAll(arbitrary[Metric]) {
        metric =>
          val userAnswers = emptyUserAnswers
            .setValue(MetricPage(index), metric)

          val result = QuantityDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

          result.left.value.page mustBe QuantityPage(index)
          result.left.value.pages mustBe Seq(
            MetricPage(index),
            QuantityPage(index)
          )
      }
    }
  }

}
