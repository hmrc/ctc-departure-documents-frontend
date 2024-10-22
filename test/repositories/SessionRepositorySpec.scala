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

package repositories

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.CacheConnector
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock

import scala.concurrent.Future

class SessionRepositorySpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockCacheConnector: CacheConnector = mock[CacheConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCacheConnector)
  }

  private val repository: SessionRepository = new SessionRepository(mockCacheConnector)

  "get" - {
    "must call connector" in {
      val userAnswers = emptyUserAnswers

      when(mockCacheConnector.get(any())(any())).thenReturn(Future.successful(Some(userAnswers)))

      val result = repository.get(lrn).futureValue

      result.value.mustBe(userAnswers)

      verify(mockCacheConnector).get(eqTo(lrn))(any())
    }
  }

  "set" - {
    "must call connector" in {
      val userAnswers = emptyUserAnswers

      when(mockCacheConnector.post(any())(any())).thenReturn(Future.successful(true))

      val result = repository.set(userAnswers).futureValue

      result.mustBe(true)

      verify(mockCacheConnector).post(eqTo(userAnswers))(any())
    }
  }
}
