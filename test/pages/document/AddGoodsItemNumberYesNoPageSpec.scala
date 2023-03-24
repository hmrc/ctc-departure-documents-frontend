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

import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddGoodsItemNumberYesNoPageSpec extends PageBehaviours {

  "AddGoodsItemNumberYesNoPage" - {

    beRetrievable[Boolean](AddGoodsItemNumberYesNoPage(documentIndex))

    beSettable[Boolean](AddGoodsItemNumberYesNoPage(documentIndex))

    beRemovable[Boolean](AddGoodsItemNumberYesNoPage(documentIndex))

    "cleanup" - {
      "when NO selected" - {
        "must clean up goods item number page at document index" in {
          forAll(arbitrary[Int]) {
            goodsItemNumber =>
              val preChange = emptyUserAnswers
                .setValue(GoodsItemNumberPage(documentIndex), goodsItemNumber)

              val postChange = preChange.setValue(AddGoodsItemNumberYesNoPage(documentIndex), false)

              postChange.get(GoodsItemNumberPage(documentIndex)) mustNot be(defined)
          }
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          forAll(arbitrary[Int]) {
            goodsItemNumber =>
              val preChange = emptyUserAnswers
                .setValue(GoodsItemNumberPage(documentIndex), goodsItemNumber)

              val postChange = preChange.setValue(AddGoodsItemNumberYesNoPage(documentIndex), true)

              postChange.get(GoodsItemNumberPage(documentIndex)) must be(defined)
          }
        }
      }
    }
  }
}
