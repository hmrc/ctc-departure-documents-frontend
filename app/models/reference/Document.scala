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

package models.reference

import cats.Order
import config.FrontendAppConfig
import models.{DocumentType, Selectable}
import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class Document(`type`: DocumentType, code: String, description: String) extends Selectable {

  override def toString: String = s"${`type`.display} - ($code) $description"

  override val value: String = toString
}

object Document {

  def reads(`type`: DocumentType, config: FrontendAppConfig): Reads[Document] =
    val (codeField, descriptionField) = if (config.phase6Enabled) ("key", "value") else ("code", "description")
    (
      (__ \ codeField).read[String] and
        (__ \ descriptionField).read[String]
    )(
      (code, description) => Document(`type`, code, description)
    )

  implicit val format: Format[Document] = Json.format[Document]

  implicit val order: Order[Document] = (x: Document, y: Document) => (x, y).compareBy(_.toString)
}
