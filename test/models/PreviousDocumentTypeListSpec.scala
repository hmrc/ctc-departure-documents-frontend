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

package models

import base.SpecBase
import generators.Generators
import models.reference.{PackageType, PreviousDocumentType}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class PreviousDocumentTypeListSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "getPreviousReferencesDocumentType" - {
    "return a previousReferencesDocumentType if it exists" in {
      forAll(nonEmptyListOf[PreviousDocumentType](10)) {
        previousDocumentTypes =>
          val previousDocumentTypeList = PreviousDocumentTypeList(previousDocumentTypes.toList)

          val previousDocumentTypeCode = previousDocumentTypes.head.code

          previousDocumentTypeList.getPreviousReferencesDocumentType(previousDocumentTypeCode).value mustEqual previousDocumentTypes.head
      }
    }

    "return a None if it does not exists" in {
      val previousDocumentTypes = Seq(
        PreviousDocumentType("01", Some("previousDocumentType1")),
        PreviousDocumentType("02", Some("previousDocumentType2")),
        PreviousDocumentType("03", Some("previousDocumentType3"))
      )

      val previousDocumentTypeList = PreviousDocumentTypeList(previousDocumentTypes)

      val previousDocumentTypeCode = "04"

      previousDocumentTypeList.getPreviousReferencesDocumentType(previousDocumentTypeCode) mustBe None
    }
  }
}
