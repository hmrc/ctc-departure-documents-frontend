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

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ReferenceDataConnector
import models.DocumentType._
import models.SelectableList
import models.reference.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.inject.bind
import play.api.test.Helpers._

import scala.concurrent.Future

class DocumentsServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  private val document1 = Document(Transport, "N741", Some("Master airwaybill"))
  private val document2 = Document(Support, "C673", Some("Catch certificate"))
  private val document3 = Document(Previous, "C605", Some("Information sheet INF3"))

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)

    when(mockRefDataConnector.getDocuments()(any(), any()))
      .thenReturn(Future.successful(Seq(document1, document2)))

    when(mockRefDataConnector.getPreviousDocuments()(any(), any()))
      .thenReturn(Future.successful(Seq(document3)))

    super.beforeEach()
  }

  "DocumentTypesService" - {

    "getDocuments" - {
      "when post-transition" - {
        "must return a list of sorted document types with transport documents removed" in {
          val app = postTransitionApplicationBuilder()
            .overrides(bind[ReferenceDataConnector].toInstance(mockRefDataConnector))
            .build()

          running(app) {
            val service = app.injector.instanceOf[DocumentsService]

            service.getDocuments().futureValue mustBe
              SelectableList(Seq(document2, document3))

            verify(mockRefDataConnector).getDocuments()(any(), any())
            verify(mockRefDataConnector).getPreviousDocuments()(any(), any())
          }
        }
      }

      "when not post-transition" - {
        "must return a list of sorted document types" in {
          val app = transitionApplicationBuilder()
            .overrides(bind[ReferenceDataConnector].toInstance(mockRefDataConnector))
            .build()

          running(app) {
            val service = app.injector.instanceOf[DocumentsService]

            service.getDocuments().futureValue mustBe
              SelectableList(Seq(document2, document3, document1))

            verify(mockRefDataConnector).getDocuments()(any(), any())
            verify(mockRefDataConnector).getPreviousDocuments()(any(), any())
          }
        }
      }
    }

  }
}
