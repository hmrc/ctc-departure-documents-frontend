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

package views

import forms.AddAnotherFormProvider
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.AddAnotherDocumentViewModel
import views.behaviours.ListWithActionsViewBehaviours
import views.html.AddAnotherDocumentView

class AddAnotherDocumentViewSpec extends ListWithActionsViewBehaviours {

  private val viewModel                                          = arbitrary[AddAnotherDocumentViewModel].sample.value
  override val notMaxedOutViewModel: AddAnotherDocumentViewModel = viewModel.copy(allowMore = true)
  override val maxedOutViewModel: AddAnotherDocumentViewModel    = viewModel.copy(allowMore = false)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherDocumentView]
      .apply(form, lrn, notMaxedOutViewModel)(fakeRequest, messages)

  override def applyMaxedOutView: HtmlFormat.Appendable = {
    val form = new AddAnotherFormProvider()(prefix)
    injector
      .instanceOf[AddAnotherDocumentView]
      .apply(form, lrn, maxedOutViewModel)(fakeRequest, messages)
  }

  override val prefix: String = "addAnotherDocument"

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Documents")

  behave like pageWithMoreItemsAllowed(notMaxedOutViewModel)

  behave like pageWithItemsMaxedOutWithRadioItems(maxedOutViewModel)

  behave like pageWithContent("p", "Once you’ve added all your documents here, you can then attach them to the relevant items in your Items section.")

  behave like pageWithSubmitButton("Save and continue")

}
