package views.document

import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.document.AddSupportingItemNumberYesNoView

class AddSupportingItemNumberYesNoViewSpec extends YesNoViewBehaviours {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[AddSupportingItemNumberYesNoView].apply(form, lrn, NormalMode)(fakeRequest, messages)

  override val prefix: String = "document.addSupportingItemNumberYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")
}
