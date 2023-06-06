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
import models.reference.Document
import models.{DeclarationType, NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputSelectViewBehaviours
import views.html.document.PreviousDocumentTypeView

class PreviousDocumentTypeViewSpec extends InputSelectViewBehaviours[Document] {

  private val declarationType = arbitrary[DeclarationType].sample.get

  override def form: Form[Document] = new SelectableFormProvider()(prefix, SelectableList(values))

  override def applyView(form: Form[Document]): HtmlFormat.Appendable =
    injector.instanceOf[PreviousDocumentTypeView].apply(form, lrn, values, NormalMode, declarationType, documentIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[Document] = arbitraryPreviousDocument

  override val prefix: String = "document.previousDocumentType"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Documents")

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithContent("p", s"You need to provide this as you’re creating a $declarationType declaration departing from Great Britain.")

  behave like pageWithContent("p", "You will be able to attach this document to the relevant item in your Items section.")

  behave like pageWithSubmitButton("Save and continue")
}
