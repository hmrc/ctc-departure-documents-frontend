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
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import models.SelectableList
import models.reference.Metric
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MetricsServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new MetricsService(mockRefDataConnector)

  private val metric1 = Metric("DTN", "Hectokilogram")
  private val metric2 = Metric("GRM", "Gram")
  private val metric3 = Metric("CTM", "Carats (one metric carat = 2 x 10-4kg)")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "MetricsService" - {

    "getMetrics" - {
      "must return a list of sorted metrics" in {

        when(mockRefDataConnector.getMetrics()(any(), any()))
          .`thenReturn`(Future.successful(NonEmptySet.of(metric1, metric2, metric3)))

        service.getMetrics().futureValue mustBe
          SelectableList(Seq(metric3, metric2, metric1))

        verify(mockRefDataConnector).getMetrics()(any(), any())
      }
    }

  }
}
