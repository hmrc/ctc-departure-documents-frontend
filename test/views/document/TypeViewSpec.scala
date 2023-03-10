package views.document

import forms.DocumentTypeFormProvider
import views.behaviours.InputSelectViewBehaviours
import models.NormalMode
import models.reference.DocumentType
import models.DocumentTypeList
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.document.TypeView

class TypeViewSpec extends InputSelectViewBehaviours[DocumentType] {

  override def form: Form[DocumentType] = new DocumentTypeFormProvider()(prefix, DocumentTypeList(values))

  override def applyView(form: Form[DocumentType]): HtmlFormat.Appendable =
    injector.instanceOf[TypeView].apply(form, lrn, values, NormalMode)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[DocumentType] = arbitraryDocumentType

  override val prefix: String = "document.type"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithHint("What tpe of document do you want to add hint")

  behave like pageWithContent("label", "What tpe of document do you want to add label")

  behave like pageWithSubmitButton("Save and continue")
}
