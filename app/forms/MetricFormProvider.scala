package forms

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import models.reference.Metric
import models.MetricList

class MetricFormProvider @Inject() extends Mappings {

  def apply(prefix: String, metrics: MetricList): Form[Metric] =

    Form(
      "value" -> text(s"$prefix.error.required")
        .verifying(s"$prefix.error.required", value => metrics.getAll.exists(_.id == value))
        .transform[Metric](value => metrics.getMetric(value).get, _.id)
    )
}
