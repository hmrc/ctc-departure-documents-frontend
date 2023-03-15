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

package services

import base.SpecBase
import connectors.ReferenceDataConnector
import models.{PackageTypeList, PreviousDocumentTypeList}
import models.reference.{PackageType, PreviousDocumentType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PreviousDocumentServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new PreviousDocumentService(mockRefDataConnector)

  private val documentType1 = PreviousDocumentType("1", Some("Certificate of quality"))
  private val documentType2 = PreviousDocumentType("2", Some("Bill of lading"))
  private val documentType3 = PreviousDocumentType("3", Some("Certificate of conformity"))
  private val documentType4 = PreviousDocumentType("4", None)

  private val packageType1 = PackageType("YN", Some("Composite packaging, glass receptacle in steel drum"))
  private val packageType2 = PackageType("RD", Some("Rod"))
  private val packageType3 = PackageType("RG", Some("Ring"))

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "PreviousDocumentService" - {

    "getPreviousReferencesDocumentTypes" - {
      "must return a list of sorted previous document types" in {

        when(mockRefDataConnector.getPreviousReferencesDocumentTypes()(any(), any()))
          .thenReturn(Future.successful(Seq(documentType1, documentType2, documentType3, documentType4)))

        service.getPreviousDocumentTypes().futureValue mustBe
          PreviousDocumentTypeList(Seq(documentType4, documentType2, documentType3, documentType1))

        verify(mockRefDataConnector).getPreviousReferencesDocumentTypes()(any(), any())
      }
    }

    "getPackageTypes" - {
      "must return a list of sorted package types" in {

        when(mockRefDataConnector.getPackageTypes()(any(), any()))
          .thenReturn(Future.successful(Seq(packageType1, packageType2, packageType3)))

        service.getPackageTypes().futureValue mustBe
          PackageTypeList(Seq(packageType1, packageType3, packageType2))

        verify(mockRefDataConnector).getPackageTypes()(any(), any())
      }
    }

  }
}
