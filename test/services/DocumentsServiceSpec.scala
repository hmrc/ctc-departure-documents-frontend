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

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import models.DocumentType.*
import models.SelectableList
import models.reference.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DocumentsServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new DocumentsService(mockRefDataConnector)
  private val transportDocument                            = Document(Transport, "N741", "Master airwaybill")
  private val supportingDocument                           = Document(Support, "C673", "Catch certificate")
  private val previousDocument                             = Document(Previous, "C605", "Information sheet INF3")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)

    when(mockRefDataConnector.getSupportingDocuments()(any(), any()))
      .thenReturn(Future.successful(Right(NonEmptySet.of(supportingDocument))))

    when(mockRefDataConnector.getTransportDocuments()(any(), any()))
      .thenReturn(Future.successful(Right(NonEmptySet.of(transportDocument))))

    when(mockRefDataConnector.getPreviousDocuments()(any(), any()))
      .thenReturn(Future.successful(Right(NonEmptySet.of(previousDocument))))

    super.beforeEach()
  }

  "DocumentTypesService" - {

    "getDocuments" - {
      "and adding document at consignment level" - {
        "must return a list of sorted document types" in {
          service.getDocuments(attachToAllItems = true).futureValue mustBe
            SelectableList(Seq(previousDocument, supportingDocument, transportDocument))

          verify(mockRefDataConnector).getSupportingDocuments()(any(), any())
          verify(mockRefDataConnector).getPreviousDocuments()(any(), any())
          verify(mockRefDataConnector).getTransportDocuments()(any(), any())
        }
      }

      "and adding document at item level" - {
        "must return a list of sorted document types with transport documents removed" in {
          service.getDocuments(attachToAllItems = false).futureValue mustBe
            SelectableList(Seq(previousDocument, supportingDocument))

          verify(mockRefDataConnector).getSupportingDocuments()(any(), any())
          verify(mockRefDataConnector).getPreviousDocuments()(any(), any())
          verify(mockRefDataConnector, never()).getTransportDocuments()(any(), any())
        }
      }
    }
  }
}
