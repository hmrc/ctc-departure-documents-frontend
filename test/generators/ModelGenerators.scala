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

import config.Constants.CountryCode._
import models.LockCheck.{LockCheckFailure, Locked, Unlocked}
import models._
import models.reference._
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

  implicit lazy val arbitraryPackageType: Arbitrary[PackageType] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- Gen.option(nonEmptyString)
      } yield PackageType(code, desc)
    }

  implicit lazy val arbitraryDocument: Arbitrary[Document] =
    Arbitrary {
      for {
        documentType <- arbitrary[DocumentType]
        code         <- nonEmptyString
        desc         <- Gen.option(nonEmptyString)
      } yield Document(documentType, code, desc)
    }

  lazy val arbitraryTransportDocument: Arbitrary[Document] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- Gen.option(nonEmptyString)
      } yield Document(DocumentType.Transport, code, desc)
    }

  lazy val arbitrarySupportDocument: Arbitrary[Document] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- Gen.option(nonEmptyString)
      } yield Document(DocumentType.Support, code, desc)
    }

  lazy val arbitraryPreviousDocument: Arbitrary[Document] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- Gen.option(nonEmptyString)
      } yield Document(DocumentType.Previous, code, desc)
    }

  implicit lazy val arbitraryCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- stringsWithMaxLength(stringMaxLength)
        name        <- stringsWithMaxLength(stringMaxLength)
        phoneNumber <- Gen.option(stringsWithMaxLength(stringMaxLength))
      } yield CustomsOffice(id, name, phoneNumber)
    }

  lazy val arbitraryGbCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- stringsWithMaxLength(stringMaxLength)
        name        <- stringsWithMaxLength(stringMaxLength)
        phoneNumber <- Gen.option(stringsWithMaxLength(stringMaxLength))
      } yield CustomsOffice(s"$GB$id", name, phoneNumber)
    }

  lazy val arbitraryXiCustomsOffice: Arbitrary[CustomsOffice] =
    Arbitrary {
      for {
        id          <- stringsWithMaxLength(stringMaxLength)
        name        <- stringsWithMaxLength(stringMaxLength)
        phoneNumber <- Gen.option(stringsWithMaxLength(stringMaxLength))
      } yield CustomsOffice(s"$XI$id", name, phoneNumber)
    }

  lazy val arbitraryDeclarationType: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("T", "T1", "T2", "T2F", "TIR")
    }

  lazy val arbitraryT2OrT2FDeclarationType: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("T2", "T2F")
    }

  lazy val arbitraryNonT2OrT2FDeclarationType: Arbitrary[String] =
    Arbitrary {
      Gen.oneOf("T", "T1", "TIR")
    }

  implicit lazy val arbitraryDocumentType: Arbitrary[DocumentType] =
    Arbitrary {
      Gen.oneOf(DocumentType.values)
    }

  lazy val arbitraryIncompleteTaskStatus: Arbitrary[TaskStatus] = Arbitrary {
    Gen.oneOf(TaskStatus.InProgress, TaskStatus.NotStarted, TaskStatus.CannotStartYet)
  }

  implicit def arbitrarySelectableList[T <: Selectable](implicit arbitrary: Arbitrary[T]): Arbitrary[SelectableList[T]] = Arbitrary {
    for {
      values <- listWithMaxLength[T]()
    } yield SelectableList(values.distinctBy(_.value))
  }

  implicit lazy val arbitraryMetric: Arbitrary[Metric] =
    Arbitrary {
      for {
        code <- nonEmptyString
        desc <- nonEmptyString
      } yield Metric(code, desc)
    }

  implicit lazy val arbitraryMetricList: Arbitrary[MetricList] = Arbitrary {
    for {
      metrics <- listWithMaxLength[Metric]()
    } yield MetricList(metrics.distinctBy(_.code))
  }

  implicit lazy val arbitraryLockCheck: Arbitrary[LockCheck] =
    Arbitrary {
      Gen.oneOf(Locked, Unlocked, LockCheckFailure)
    }
}
