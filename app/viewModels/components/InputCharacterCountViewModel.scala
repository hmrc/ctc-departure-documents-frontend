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

package viewModels.components

import play.twirl.api.Html

sealed trait InputCharacterCountViewModel

object InputCharacterCountViewModel {

  def apply(
    heading: String,
    caption: Option[String],
    additionalHtml: Option[Html]
  ): InputCharacterCountViewModel = additionalHtml match {
    case Some(value) => InputCharacterCountWithAdditionalHtml(heading, caption, value)
    case None        => OrdinaryInputCharacterCount(heading, caption)
  }

  case class OrdinaryInputCharacterCount(
    heading: String,
    caption: Option[String] = None
  ) extends InputCharacterCountViewModel

  case class InputCharacterCountWithAdditionalHtml(
    heading: String,
    caption: Option[String] = None,
    additionalHtml: Html
  ) extends InputCharacterCountViewModel
      with AdditionalHtmlViewModel
}
