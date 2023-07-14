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

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.behaviours.StringFieldBehaviours
import models.domain.StringFieldRegex.alphaNumericRegex
import org.scalacheck.{Arbitrary, Gen}
import play.api.Application
import play.api.data.FormError
import play.api.test.Helpers.running

class DocumentReferenceNumberFormProviderSpec extends StringFieldBehaviours with SpecBase with AppWithDefaultMockFixtures {

  private val prefix      = Gen.alphaNumStr.sample.value
  private val requiredKey = s"$prefix.error.required"
  private val lengthKey   = s"$prefix.error.length"
  private val invalidKey  = s"$prefix.error.invalidCharacters"
  private val uniqueKey   = s"$prefix.error.unique"

  "when post-transition" - {
    val app = postTransitionApplicationBuilder().build()
    running(app) {
      runTest(app, 70)
    }
  }

  "when during transition" - {
    val app = transitionApplicationBuilder().build()
    running(app) {
      runTest(app, 35)
    }
  }

  private def runTest(app: Application, maxLength: Int): Unit = {

    val values = listWithMaxLength[String]()(Arbitrary(stringsWithMaxLength(maxLength))).sample.value

    val formProvider = app.injector.instanceOf[DocumentReferenceNumberFormProvider]
    val form         = formProvider(prefix, values)

    ".value" - {

      val fieldName = "value"

      behave like fieldThatBindsValidData(
        form = form,
        fieldName = fieldName,
        validDataGenerator = stringsWithMaxLength(maxLength)
      )

      behave like fieldWithMaxLength(
        form = form,
        fieldName = fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
      )

      behave like mandatoryField(
        form = form,
        fieldName = fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )

      behave like fieldWithInvalidCharacters(
        form = form,
        fieldName = fieldName,
        error = FormError(fieldName, invalidKey, Seq(alphaNumericRegex.regex)),
        length = maxLength
      )

      behave like fieldThatBindsUniqueData(
        form = form,
        fieldName = fieldName,
        uniqueError = FormError(fieldName, uniqueKey),
        values = values
      )
    }
  }
}
