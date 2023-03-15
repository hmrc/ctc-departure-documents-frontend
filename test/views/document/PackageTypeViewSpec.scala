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

import forms.PackageTypeFormProvider
import views.behaviours.InputSelectViewBehaviours
import models.NormalMode
import models.reference.DocumentType
import models.DocumentTypeList
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.document.PackageTypeView

class PackageTypeViewSpec extends InputSelectViewBehaviours[DocumentType] {

  override def form: Form[DocumentType] = new PackageTypeFormProvider()(prefix, DocumentTypeList(values))

  override def applyView(form: Form[DocumentType]): HtmlFormat.Appendable =
    injector.instanceOf[PackageTypeView].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[DocumentType] = arbitraryDocumentType

  override val prefix: String = "document.packageType"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithHint("What type of package did the goods arrive in? hint")

  behave like pageWithContent("label", "What type of package did the goods arrive in? label")

  behave like pageWithSubmitButton("Save and continue")
}
