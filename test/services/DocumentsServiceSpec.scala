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
import models.DocumentType._
import models.SelectableList
import models.reference.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DocumentsServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new DocumentsService(mockRefDataConnector)

  private val document1 = Document(Support, "1", Some("CERTIFICATE OF QUALITY"), UUID.randomUUID())
  private val document2 = Document(Support, "2", Some("Bill of lading"), UUID.randomUUID())
  private val document3 = Document(Previous, "3", Some("Certificate of conformity"), UUID.randomUUID())

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "DocumentTypesService" - {

    "getDocuments" - {
      "must return a list of sorted document types" in {

        when(mockRefDataConnector.getDocuments()(any(), any()))
          .thenReturn(Future.successful(Seq(document1, document2)))

        when(mockRefDataConnector.getPreviousDocuments()(any(), any()))
          .thenReturn(Future.successful(Seq(document3)))

        service.getDocuments().futureValue mustBe
          SelectableList(Seq(document2, document3, document1))

        verify(mockRefDataConnector).getDocuments()(any(), any())
        verify(mockRefDataConnector).getPreviousDocuments()(any(), any())
      }
    }

  }
}
