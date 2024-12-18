/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.mappings

import base.SpecBase
import generators.Generators
import models.ConsignmentLevelDocuments
import models.reference.Document
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.validation.{Invalid, Valid}

import java.time.LocalDate

class ConstraintsSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with Constraints {

  "firstError" - {

    "must return Valid when all constraints pass" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""".r, "error.regexp"))("foo")
      result mustEqual Valid
    }

    "must return Invalid when the first constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""".r, "error.regexp"))("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }

    "must return Invalid when the second constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""".r, "error.regexp"))("")
      result mustEqual Invalid("error.regexp", """^\w+$""")
    }

    "must return Invalid for the first error when both constraints fail" in {
      val result = firstError(maxLength(-1, "error.length"), regexp("""^\w+$""".r, "error.regexp"))("")
      result mustEqual Invalid("error.length", -1)
    }
  }

  "minimumValue" - {

    "must return Valid for a number greater than the threshold" in {
      val result = minimumValue(1, "error.min").apply(2)
      result mustEqual Valid
    }

    "must return Valid for a number equal to the threshold" in {
      val result = minimumValue(1, "error.min").apply(1)
      result mustEqual Valid
    }

    "must return Invalid for a number below the threshold" in {
      val result = minimumValue(1, "error.min").apply(0)
      result mustEqual Invalid("error.min", 1)
    }

    "must return Invalid for a number below the threshold when number is negative" in {
      val result = minimumValue(0, "error.min").apply(-1)
      result mustEqual Invalid("error.min", 0)
    }
  }

  "maximumValue" - {

    "must return Valid for a number less than the threshold" in {
      val result = maximumValue(1, "error.max").apply(0)
      result mustEqual Valid
    }

    "must return Valid for a number equal to the threshold" in {
      val result = maximumValue(1, "error.max").apply(1)
      result mustEqual Valid
    }

    "must return Invalid for a number above the threshold" in {
      val result = maximumValue(1, "error.max").apply(2)
      result mustEqual Invalid("error.max", 1)
    }
  }

  "regexp" - {

    "must return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""".r, "error.invalid")("foo")
      result mustEqual Valid
    }

    "must return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""".r, "error.invalid")("foo")
      result mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" - {

    "must return Valid for a string shorter than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 9)
      result mustEqual Valid
    }

    "must return Valid for an empty string" in {
      val result = maxLength(10, "error.length")("")
      result mustEqual Valid
    }

    "must return Valid for a string equal to the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 10)
      result mustEqual Valid
    }

    "must return Invalid for a string longer than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }
  }

  "maxDate" - {

    "must return Valid for a date before or equal to the maximum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), max)
      } yield (max, date)

      forAll(gen) {
        case (max, date) =>
          val result = maxDate(max, "error.future")(date)
          result mustEqual Valid
      }
    }

    "must return Invalid for a date after the maximum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(max.plusDays(1), LocalDate.of(3000, 1, 2))
      } yield (max, date)

      forAll(gen) {
        case (max, date) =>
          val result = maxDate(max, "error.future", "foo")(date)
          result mustEqual Invalid("error.future", "foo")
      }
    }
  }

  "minDate" - {

    "must return Valid for a date after or equal to the minimum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        min  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(min, LocalDate.of(3000, 1, 1))
      } yield (min, date)

      forAll(gen) {
        case (min, date) =>
          val result = minDate(min, "error.past", "foo")(date)
          result mustEqual Valid
      }
    }

    "must return Invalid for a date before the minimum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        min  <- datesBetween(LocalDate.of(2000, 1, 2), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), min.minusDays(1))
      } yield (min, date)

      forAll(gen) {
        case (min, date) =>
          val result = minDate(min, "error.past", "foo")(date)
          result mustEqual Invalid("error.past", "foo")
      }
    }
  }

  "maxLimit" - {

    "must return Valid" - {
      "when adding a consignment-level previous document won't take me over the limit" in {
        forAll(
          Gen.choose(0, frontendAppConfig.maxPreviousDocuments - 1),
          arbitrary[Document](arbitraryPreviousDocument)
        ) {
          (numberOfPreviousDocuments, document) =>
            val consignmentLevelDocuments = ConsignmentLevelDocuments(numberOfPreviousDocuments, 0, 0)
            val result                    = maxLimit(consignmentLevelDocuments, attachedToAllItems = true, "error.maxLimitReached")(frontendAppConfig)(document)
            result mustEqual Valid
        }
      }

      "when adding a consignment-level supporting document won't take me over the limit" in {
        forAll(
          Gen.choose(0, frontendAppConfig.maxSupportingDocuments - 1),
          arbitrary[Document](arbitrarySupportDocument)
        ) {
          (numberOfSupportingDocuments, document) =>
            val consignmentLevelDocuments = ConsignmentLevelDocuments(0, numberOfSupportingDocuments, 0)
            val result                    = maxLimit(consignmentLevelDocuments, attachedToAllItems = true, "error.maxLimitReached")(frontendAppConfig)(document)
            result mustEqual Valid
        }
      }

      "when adding a consignment-level transport document won't take me over the limit" in {
        forAll(
          Gen.choose(0, frontendAppConfig.maxTransportDocuments - 1),
          arbitrary[Document](arbitraryTransportDocument)
        ) {
          (numberOfTransportDocuments, document) =>
            val consignmentLevelDocuments = ConsignmentLevelDocuments(0, 0, numberOfTransportDocuments)
            val result                    = maxLimit(consignmentLevelDocuments, attachedToAllItems = true, "error.maxLimitReached")(frontendAppConfig)(document)
            result mustEqual Valid
        }
      }

      "when adding a non-consignment-level document" in {
        forAll(arbitrary[Document]) {
          document =>
            val consignmentLevelDocuments = ConsignmentLevelDocuments(frontendAppConfig.maxPreviousDocuments,
                                                                      frontendAppConfig.maxSupportingDocuments,
                                                                      frontendAppConfig.maxTransportDocuments
            )
            val result = maxLimit(consignmentLevelDocuments, attachedToAllItems = false, "error.maxLimitReached")(frontendAppConfig)(document)
            result mustEqual Valid
        }
      }
    }

    "must return Invalid" - {
      "when adding a consignment-level previous document will take me over the limit" in {
        forAll(
          arbitrary[Document](arbitraryPreviousDocument)
        ) {
          document =>
            val consignmentLevelDocuments = ConsignmentLevelDocuments(frontendAppConfig.maxPreviousDocuments, 0, 0)
            val result                    = maxLimit(consignmentLevelDocuments, attachedToAllItems = true, "error.maxLimitReached")(frontendAppConfig)(document)
            result mustEqual Invalid("error.maxLimitReached")
        }
      }

      "when adding a consignment-level supporting document will take me over the limit" in {
        forAll(
          arbitrary[Document](arbitrarySupportDocument)
        ) {
          document =>
            val consignmentLevelDocuments = ConsignmentLevelDocuments(0, frontendAppConfig.maxSupportingDocuments, 0)
            val result                    = maxLimit(consignmentLevelDocuments, attachedToAllItems = true, "error.maxLimitReached")(frontendAppConfig)(document)
            result mustEqual Invalid("error.maxLimitReached")
        }
      }

      "when adding a consignment-level transport document will take me over the limit" in {
        forAll(
          arbitrary[Document](arbitraryTransportDocument)
        ) {
          document =>
            val consignmentLevelDocuments = ConsignmentLevelDocuments(0, 0, frontendAppConfig.maxTransportDocuments)
            val result                    = maxLimit(consignmentLevelDocuments, attachedToAllItems = true, "error.maxLimitReached")(frontendAppConfig)(document)
            result mustEqual Invalid("error.maxLimitReached")
        }
      }
    }
  }

  "notInList" - {
    "must return Valid for a value not in the list" in {

      val values = Seq("foo", "bar")

      val result = notInList(values, "error.unique")("baz")
      result mustEqual Valid
    }

    "must return Invalid for a value in the list" in {

      val values = Seq("foo", "bar")

      val result = notInList(values, "error.unique")("foo")
      result mustEqual Invalid("error.unique")
    }
  }
}
