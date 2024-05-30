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

class DeclareQuantityOfGoodsYesNoPageSpec extends PageBehaviours {

  "DeclareQuantityOfGoodsYesNoPage" - {

    beRetrievable[Boolean](DeclareQuantityOfGoodsYesNoPage(documentIndex))

    beSettable[Boolean](DeclareQuantityOfGoodsYesNoPage(documentIndex))

    beRemovable[Boolean](DeclareQuantityOfGoodsYesNoPage(documentIndex))

    "cleanup" - {
      "when no selected" - {
        "must remove metric and quantity" in {
          val userAnswers = emptyUserAnswers
            .setValue(DeclareQuantityOfGoodsYesNoPage(index), true)
            .setValue(MetricPage(index), arbitrary[Metric].sample.value)
            .setValue(QuantityPage(index), arbitrary[BigDecimal].sample.value)

          val result = userAnswers.setValue(DeclareQuantityOfGoodsYesNoPage(index), false)

          result.get(MetricPage(index)) must not be defined
          result.get(QuantityPage(index)) must not be defined
        }
      }
    }
  }
}
