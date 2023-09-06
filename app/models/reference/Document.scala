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

import models.{DocumentType, Selectable}
import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

case class Document(`type`: DocumentType, code: String, description: Option[String]) extends Selectable {

  override def asString(implicit messages: Messages): String = description match {
    case Some(value) if value.trim.nonEmpty => s"${`type`.asString} - ($code) $value"
    case _                                  => s"${`type`.asString} - $code"
  }

  override val value: String = toString
}

object Document {

  def httpReads(`type`: DocumentType): HttpReads[Seq[Document]] = (_: String, _: String, response: HttpResponse) => {
    val referenceData: JsValue = (response.json \ "data").getOrElse(
      throw new IllegalStateException("[Document][httpReads] Reference data could not be parsed")
    )

    referenceData match {
      case JsArray(values) =>
        values.flatMap(_.validate[Document](referenceDataReads(`type`)).asOpt).toSeq
      case _ =>
        Nil
    }
  }

  def referenceDataReads(`type`: DocumentType): Reads[Document] = (
    (__ \ "code").read[String] and
      (__ \ "description").readNullable[String]
  ).apply {
    (code, description) =>
      Document(`type`, code, description)
  }

  implicit val format: Format[Document] = Json.format[Document]
}
