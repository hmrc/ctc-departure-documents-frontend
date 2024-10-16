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

package config

import models.Phase
import models.Phase.{PostTransition, Transition}

trait PhaseConfig {
  val phase: Phase
  val maxDocumentRefNumberLength: Int
  val maxAdditionalInformationLength: Int
}

class TransitionConfig() extends PhaseConfig {
  override val phase: Phase                        = Transition
  override val maxDocumentRefNumberLength: Int     = 35
  override val maxAdditionalInformationLength: Int = 26
}

class PostTransitionConfig() extends PhaseConfig {
  override val phase: Phase                        = PostTransition
  override val maxDocumentRefNumberLength: Int     = 70
  override val maxAdditionalInformationLength: Int = 35
}
