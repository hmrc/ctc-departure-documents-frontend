package forms

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import models.reference.DocumentType
import models.DocumentTypeList

class DocumentTypeFormProvider @Inject() extends Mappings {

  def apply(prefix: String, documentTypes: DocumentTypeList): Form[DocumentType] =

    Form(
      "value" -> text(s"$prefix.error.required")
        .verifying(s"$prefix.error.required", value => documentTypes.getAll.exists(_.id == value))
        .transform[DocumentType](value => documentTypes.getDocumentType(value).get, _.id)
    )
}
