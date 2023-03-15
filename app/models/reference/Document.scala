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

package models.reference

import models.Foo._
import models.{Foo, Selectable}
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Format, Json, Reads}

case class Document(`type`: Foo, code: String, description: Option[String]) extends Selectable {

  override def toString: String = description match {
    case Some(value) if value.trim.nonEmpty => s"($code) $value"
    case _                                  => code
  }

  override val value: String = code
}

object Document {

  val referenceDataReads: Reads[Document] = (
    (__ \ "code").read[String] and
      (__ \ "description").readNullable[String] and
      (__ \ "transportDocument").readNullable[Boolean]
  ).apply {
    (code, description, isTransportDocument) =>
      val `type` = isTransportDocument match {
        case Some(true)  => Transport
        case Some(false) => Support
        case None        => Previous
      }
      Document(`type`, code, description)
  }

  implicit val format: Format[Document] = Json.format[Document]
}
