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
import models.reference.PackageType
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class PackageTypeListSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "getPackageType" - {
    "return a packageType if it exists" in {
      forAll(nonEmptyListOf[PackageType](10)) {
        packageTypes =>
          val packageTypeList = PackageTypeList(packageTypes.toList)

          val packageTypeCode = packageTypes.head.code

          packageTypeList.getPackageType(packageTypeCode).value mustEqual packageTypes.head
      }
    }

    "return a None if it does not exists" in {
      val packageTypes = Seq(
        PackageType("01", Some("packageType1")),
        PackageType("02", Some("packageType2")),
        PackageType("03", Some("packageType3"))
      )

      val packageTypeList = PackageTypeList(packageTypes)

      val packageTypeCode = "04"

      packageTypeList.getPackageType(packageTypeCode) mustBe None
    }
  }
}
