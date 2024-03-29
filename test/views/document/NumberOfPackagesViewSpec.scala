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

import forms.IntFormProvider
import models.NormalMode
import models.reference.PackageType
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.document.NumberOfPackagesView

class NumberOfPackagesViewSpec extends InputTextViewBehaviours[Int] {

  private val packageType = Arbitrary.arbitrary[PackageType].sample.get

  override def form: Form[Int] = new IntFormProvider()(prefix, 10)

  override def applyView(form: Form[Int]): HtmlFormat.Appendable =
    injector.instanceOf[NumberOfPackagesView].apply(form, lrn, NormalMode, documentIndex, packageType.toString)(fakeRequest, messages)

  private val maxInt = 99999999

  implicit override val arbitraryT: Arbitrary[Int] = Arbitrary(maxInt)

  override val prefix: String = "document.numberOfPackages"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Documents")

  behave like pageWithHeading()

  behave like pageWithoutHint()

  behave like pageWithInsetText(packageType.toString)

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithSubmitButton("Save and continue")
}
