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

package generators

import models._
import models.reference.{DocumentType, PackageType, PreviousDocumentType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs._

trait ModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryLocalReferenceNumber: Arbitrary[LocalReferenceNumber] =
    Arbitrary {
      for {
        lrn <- stringsWithMaxLength(22: Int, Gen.alphaNumChar)
      } yield new LocalReferenceNumber(lrn)
    }

  implicit lazy val arbitraryEoriNumber: Arbitrary[EoriNumber] =
    Arbitrary {
      for {
        number <- stringsWithMaxLength(17: Int)
      } yield EoriNumber(number)
    }

  implicit lazy val arbitraryMode: Arbitrary[Mode] = Arbitrary {
    Gen.oneOf(NormalMode, CheckMode)
  }

  implicit lazy val arbitraryIndex: Arbitrary[Index] = Arbitrary {
    for {
      position <- Gen.choose(0: Int, 10: Int)
    } yield Index(position)
  }

  implicit lazy val arbitraryCall: Arbitrary[Call] = Arbitrary {
    for {
      method <- Gen.oneOf(GET, POST)
      url    <- nonEmptyString
    } yield Call(method, url)
  }

  implicit lazy val arbitraryPreviousDocumentType: Arbitrary[PreviousDocumentType] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- Gen.option(nonEmptyString)
      } yield PreviousDocumentType(code, desc)
    }

  implicit lazy val arbitraryPackageType: Arbitrary[PackageType] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- Gen.option(nonEmptyString)
      } yield PackageType(code, desc)
    }

  implicit lazy val arbitraryDocumentType: Arbitrary[DocumentType] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
        doc  <- arbitrary[Boolean]
      } yield DocumentType(code, desc, doc)
    }

  implicit lazy val arbitraryPreviousDocumentTypeList: Arbitrary[PreviousDocumentTypeList] = Arbitrary {
    for {
      previousDocumentType <- listWithMaxLength[PreviousDocumentType]()
    } yield PreviousDocumentTypeList(previousDocumentType.distinctBy(_.code))
  }

  lazy val arbitraryIncompleteTaskStatus: Arbitrary[TaskStatus] = Arbitrary {
    Gen.oneOf(TaskStatus.InProgress, TaskStatus.NotStarted, TaskStatus.CannotStartYet)
  }
}
