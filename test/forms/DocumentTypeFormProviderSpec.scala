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

package forms

import forms.behaviours.StringFieldBehaviours
import models.DocumentTypeList
import play.api.data.FormError
import generators.Generators
import org.scalacheck.Gen

class DocumentTypeFormProviderSpec extends StringFieldBehaviours with Generators {

  private val prefix      = Gen.alphaNumStr.sample.value
  private val requiredKey = s"$prefix.error.required"
  private val maxLength   = 8

  private val documentType1    = arbitraryDocumentType.arbitrary.sample.get
  private val documentType2    = arbitraryDocumentType.arbitrary.sample.get
  private val documentTypeList = DocumentTypeList(Seq(documentType1, documentType2))

  private val form = new DocumentTypeFormProvider()(prefix, documentTypeList)

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

    "not bind if documentType id does not exist in the documentTypeList" in {
      val boundForm = form.bind(Map("value" -> "foobar"))
      val field     = boundForm("value")
      field.errors mustNot be(empty)
    }

    "bind a documentType id which is in the list" in {
      val boundForm = form.bind(Map("value" -> documentType1.code))
      val field     = boundForm("value")
      field.errors must be(empty)
    }
  }
}
