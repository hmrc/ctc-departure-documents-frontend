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

import base.AppWithDefaultMockFixtures
import forms.AdditionalInformationFormProvider
import models.NormalMode
import play.api.Application
import play.api.data.Form
import play.api.test.Helpers.running
import play.twirl.api.HtmlFormat
import views.behaviours.CharacterCountViewBehaviours
import views.html.document.AdditionalInformationView

class AdditionalInformationViewSpec extends CharacterCountViewBehaviours with AppWithDefaultMockFixtures {

  override def form: Form[String] = form(app)

  private def form(app: Application): Form[String] =
    app.injector.instanceOf[AdditionalInformationFormProvider].apply(prefix, documentIndex.display)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    applyView(app, form)

  private def applyView(app: Application): HtmlFormat.Appendable =
    applyView(app, form(app))

  private def applyView(app: Application, form: Form[String]): HtmlFormat.Appendable =
    app.injector.instanceOf[AdditionalInformationView].apply(form, lrn, NormalMode, documentIndex)(fakeRequest, messages)

  override val prefix: String = "document.additionalInformation"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Documents")

  behave like pageWithHeading()

  behave like pageWithSubmitButton("Save and continue")

  "when post-transition" - {
    val app = postTransitionApplicationBuilder().build()
    running(app) {
      val doc = parseView(applyView(app))
      behave like pageWithCharacterCount(doc, 35)
    }
  }

  "when transition" - {
    val app = transitionApplicationBuilder().build()
    running(app) {
      val doc = parseView(applyView(app))
      behave like pageWithCharacterCount(doc, 26)
    }
  }
}
