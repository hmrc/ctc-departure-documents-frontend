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
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.{AddAnotherDocumentViewModel, Entity, ListItem}
import views.behaviours.ListWithActionsViewBehaviours
import views.html.AddAnotherDocumentView

class AddAnotherDocumentViewSpec extends ListWithActionsViewBehaviours[Entity.Document] {

  override def maxNumber: Int = frontendAppConfig.maxDocuments

  implicit override def arbitraryListItem: Arbitrary[ListItem[Entity.Document]] = arbitraryDocumentListItem

  private def formProvider(viewModel: AddAnotherDocumentViewModel) =
    new AddAnotherFormProvider()(viewModel.prefix, viewModel.allowMore)

  private val viewModel            = arbitrary[AddAnotherDocumentViewModel].sample.value
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherDocumentView]
      .apply(form, lrn, notMaxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyMaxedOutView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherDocumentView]
      .apply(formProvider(maxedOutViewModel), lrn, maxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override val prefix: String = "addAnotherDocument"

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Documents")

  behave like pageWithMoreItemsAllowed(notMaxedOutViewModel.count)()

  behave like pageWithItemsMaxedOut(maxedOutViewModel.count)

  behave like pageWithSubmitButton("Save and continue")

}
