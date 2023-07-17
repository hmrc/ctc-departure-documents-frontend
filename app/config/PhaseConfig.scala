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

package config

import models.Phase
import models.Phase.{PostTransition, Transition}
import play.api.Configuration

trait PhaseConfig {
  // we can put things like form validation values in here (regex, length etc.)
  val phase: Phase
  def maxPreviousDocuments(implicit configuration: Configuration): Int
}

class TransitionConfig() extends PhaseConfig {
  override val phase: Phase                                                     = Transition
  override def maxPreviousDocuments(implicit configuration: Configuration): Int = configuration.get[Int]("limits.maxPreviousDocuments.transition")
}

class PostTransitionConfig() extends PhaseConfig {
  override val phase: Phase                                                     = PostTransition
  override def maxPreviousDocuments(implicit configuration: Configuration): Int = configuration.get[Int]("limits.maxPreviousDocuments.postTransition")
}
