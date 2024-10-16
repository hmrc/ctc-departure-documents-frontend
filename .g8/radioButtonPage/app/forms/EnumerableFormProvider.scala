package forms

import forms.mappings.Mappings
import models.{Enumerable, Radioable}
import play.api.data.Form

import javax.inject.Inject

class EnumerableFormProvider @Inject() extends Mappings {

  def apply[T <: Radioable[T]](prefix: String)(implicit et: Enumerable[T]): Form[T] =
    Form(
      "value" -> enumerable[T](s"\$prefix.error.required")
    )

  def apply[T <: Radioable[T]](prefix: String, values: Seq[T])(implicit et: Seq[T] => Enumerable[T]): Form[T] =
    apply(prefix)(et(values))
}
