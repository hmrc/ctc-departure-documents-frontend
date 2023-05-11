package views.document

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.document.AdditionalInformationView

class AdditionalInformationViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[AdditionalInformationView].apply(lrn)(fakeRequest, messages)

  override val prefix: String = "document.additionalInformation"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()
}
