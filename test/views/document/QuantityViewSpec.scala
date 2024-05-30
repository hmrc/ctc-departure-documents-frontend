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

package views.document

import forms.BigDecimalFormProvider
import models.NormalMode
import models.reference.Metric
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.document.QuantityView

class QuantityViewSpec extends InputTextViewBehaviours[BigDecimal] {

  private val metric = arbitrary[Metric].sample.value

  override def form: Form[BigDecimal] = new BigDecimalFormProvider()(prefix)

  override def applyView(form: Form[BigDecimal]): HtmlFormat.Appendable =
    injector.instanceOf[QuantityView].apply(form, lrn, NormalMode, index, metric)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[BigDecimal] = Arbitrary(positiveBigDecimals)

  override val prefix: String = "document.quantity"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutHint()

  behave like pageWithInsetText(metric.toString)

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Save and continue")
}
