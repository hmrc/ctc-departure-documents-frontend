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

package navigation

import config.FrontendAppConfig
import models._
import models.journeyDomain.{DocumentDomain, UserAnswersReader}

import javax.inject.{Inject, Singleton}

@Singleton
class DocumentNavigatorProviderImpl @Inject() (implicit config: FrontendAppConfig) extends DocumentNavigatorProvider {

  def apply(mode: Mode, index: Index): UserAnswersNavigator =
    new DocumentNavigator(mode, index)
}

trait DocumentNavigatorProvider {

  def apply(mode: Mode, index: Index): UserAnswersNavigator
}

class DocumentNavigator(override val mode: Mode, index: Index)(implicit override val config: FrontendAppConfig) extends UserAnswersNavigator {

  override type T = DocumentDomain

  implicit override val reader: UserAnswersReader[DocumentDomain] =
    DocumentDomain.userAnswersReader(index).apply(Nil)
}
