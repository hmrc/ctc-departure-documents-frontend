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

import forms.Constants.maxDocumentRefNumberLength
import forms.behaviours.StringFieldBehaviours
import models.domain.StringFieldRegex.alphaNumericRegex
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.FormError

class DocumentReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  private val prefix = Gen.alphaNumStr.sample.value
  val requiredKey    = s"$prefix.error.required"
  val lengthKey      = s"$prefix.error.length"
  val invalidKey     = s"$prefix.error.invalidCharacters"
  private val uniqueKey = s"$prefix.error.unique"

  private val values = listWithMaxLength[String]()(Arbitrary(nonEmptyString)).sample.value

  val form = new DocumentReferenceNumberFormProvider()(prefix, values)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxDocumentRefNumberLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxDocumentRefNumberLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxDocumentRefNumberLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      error = FormError(fieldName, invalidKey, Seq(alphaNumericRegex.regex)),
      maxDocumentRefNumberLength
    )

    behave like fieldThatBindsUniqueData(
      form = form,
      fieldName = fieldName,
      uniqueError = FormError(fieldName, uniqueKey),
      values = values
    )
  }
}
