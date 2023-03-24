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

import generators.Generators
import models.NormalMode
import models.reference.Document
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.document.RemoveDocumentView

class RemoveDocumentViewSpec extends YesNoViewBehaviours with Generators {

  private val documentType = arbitrary[Document].sample.value

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveDocumentView]
      .apply(form, lrn, NormalMode, documentIndex, documentType)(fakeRequest, messages)

  override val prefix: String = "document.removeDocument"

  behave like pageWithTitle(documentType)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Documents")

  behave like pageWithHeading(documentType)

  behave like pageWithRadioItems(args = Seq(documentType))

  behave like pageWithSubmitButton("Save and continue")
}