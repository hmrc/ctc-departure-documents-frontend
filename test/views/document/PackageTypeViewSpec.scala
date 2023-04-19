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

import forms.SelectableFormProvider
import models.reference.PackageType
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputSelectViewBehaviours
import views.html.document.PackageTypeView

class PackageTypeViewSpec extends InputSelectViewBehaviours[PackageType] {

  override def form: Form[PackageType] = new SelectableFormProvider()(prefix, SelectableList(values))

  override def applyView(form: Form[PackageType]): HtmlFormat.Appendable =
    injector.instanceOf[PackageTypeView].apply(form, lrn, values, NormalMode, documentIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[PackageType] = arbitraryPackageType

  override val prefix: String = "document.packageType"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Documents")

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithSubmitButton("Save and continue")
}
