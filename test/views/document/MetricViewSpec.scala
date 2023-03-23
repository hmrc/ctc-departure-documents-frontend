package views.document

import forms.MetricFormProvider
import views.behaviours.InputSelectViewBehaviours
import models.NormalMode
import models.reference.Metric
import models.MetricList
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.document.MetricView

class MetricViewSpec extends InputSelectViewBehaviours[Metric] {

  override def form: Form[Metric] = new MetricFormProvider()(prefix, MetricList(values))

  override def applyView(form: Form[Metric]): HtmlFormat.Appendable =
    injector.instanceOf[MetricView].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[Metric] = arbitraryMetric

  override val prefix: String = "document.metric"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithHint("What metric do you want to use for the quantity of goods hint")

  behave like pageWithContent("label", "What metric do you want to use for the quantity of goods label")

  behave like pageWithSubmitButton("Save and continue")
}
