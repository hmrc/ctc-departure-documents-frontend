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

package controllers.actions

import models.UserAnswersResponse.{Answers, NoAnswers}
import models.requests.{IdentifierRequest, OptionalDataRequest}
import models.{LocalReferenceNumber, UserAnswers}
import play.api.mvc.ActionTransformer

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalActionProvider(dataToReturn: Option[UserAnswers]) extends DataRetrievalActionProvider {

  def apply(lrn: LocalReferenceNumber): ActionTransformer[IdentifierRequest, OptionalDataRequest] =
    new FakeDataRetrievalAction(dataToReturn)
}

class FakeDataRetrievalAction(dataToReturn: Option[UserAnswers]) extends ActionTransformer[IdentifierRequest, OptionalDataRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
    Future(OptionalDataRequest(request.request, request.eoriNumber, dataToReturn.map(Answers(_)).getOrElse(NoAnswers)))

  implicit override protected val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
