package forms

import forms.behaviours.StringFieldBehaviours
import models.MetricList
import play.api.data.FormError
import generators.Generators
import org.scalacheck.Gen

class MetricFormProviderSpec extends StringFieldBehaviours with Generators{

  private val prefix      = Gen.alphaNumStr.sample.value
  private val requiredKey = s"$prefix.error.required"
  private val maxLength   = 8

  private val metric1 = arbitraryMetric.arbitrary.sample.get
  private val metric2 = arbitraryMetric.arbitrary.sample.get
  private val metricList = MetricList(Seq(metric1, metric2))

  private val form = new MetricFormProvider()(prefix, metricList)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "not bind if customs office id does not exist in the metricList" in {
      val boundForm = form.bind(Map("value" -> "foobar"))
      val field     = boundForm("value")
      field.errors mustNot be(empty)
    }

    "bind a metric id which is in the list" in {
      val boundForm = form.bind(Map("value" -> metric1.id))
      val field     = boundForm("value")
      field.errors must be(empty)
    }
  }
}
