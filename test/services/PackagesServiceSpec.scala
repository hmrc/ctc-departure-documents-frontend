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

package services

import base.SpecBase
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import models.SelectableList
import models.reference.PackageType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PackagesServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new PackagesService(mockRefDataConnector)

  private val packageType1 = PackageType("YN", "Composite packaging, glass receptacle in steel drum")
  private val packageType2 = PackageType("RD", "Rod")
  private val packageType3 = PackageType("RG", "Ring")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "PackagesService" - {

    "getPackageTypes" - {
      "must return a list of sorted package types" in {

        when(mockRefDataConnector.getPackageTypes()(any(), any()))
          .thenReturn(Future.successful(Right(NonEmptySet.of(packageType1, packageType2, packageType3))))

        service.getPackageTypes().futureValue mustBe
          SelectableList(Seq(packageType2, packageType3, packageType1))

        verify(mockRefDataConnector).getPackageTypes()(any(), any())
      }
    }
  }
}
