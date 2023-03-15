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

package views.document

import forms.GoodsItemNumberFormProvider
import models.NormalMode
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.document.GoodsItemNumberView

class GoodsItemNumberViewSpec extends InputTextViewBehaviours[String] {

  override val prefix: String = "document.goodsItemNumber"

  override def form: Form[String] = new GoodsItemNumberFormProvider()(prefix)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[GoodsItemNumberView].apply(form, lrn, NormalMode, documentIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Documents")

  behave like pageWithHeading()

  behave like pageWithHint("This can be up to 5 numbers long.")

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Save and continue")
}
