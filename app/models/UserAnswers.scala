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

package models

import pages.QuestionPage
import pages.external.DocumentPage
import pages.sections.external.{DocumentSection, DocumentsSection, ItemsSection}
import play.api.libs.json._
import queries.Gettable

import java.util.UUID
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  lrn: LocalReferenceNumber,
  eoriNumber: EoriNumber,
  data: JsObject = Json.obj(),
  tasks: Map[String, TaskStatus] = Map()
) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def getArray(array: Gettable[JsArray]): JsArray = get(array).getOrElse(JsArray())

  def getArraySize(array: Gettable[JsArray]): Int = getArray(array).value.size

  def set[A](page: QuestionPage[A], value: A)(implicit writes: Writes[A], reads: Reads[A]): Try[UserAnswers] = {
    lazy val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    lazy val cleanup: JsObject => Try[UserAnswers] = d => {
      val updatedAnswers = copy(data = d)
      page.cleanup(Some(value), updatedAnswers)
    }

    get(page) match {
      case Some(`value`) => Success(this)
      case _             => updatedData flatMap cleanup
    }
  }

  def remove[A](page: QuestionPage[A]): Try[UserAnswers] = {
    val updatedData    = data.removeObject(page.path).getOrElse(data)
    val updatedAnswers = copy(data = updatedData)
    page.cleanup(None, updatedAnswers)
  }

  def updateTask(section: String, status: TaskStatus): UserAnswers = {
    val tasks = this.tasks.updated(section, status)
    this.copy(tasks = tasks)
  }

  def removeDocumentFromItems(uuid: Option[UUID]): UserAnswers = uuid match {
    case Some(documentUuid) =>
      val numberOfItems = this.getArraySize(ItemsSection)
      (0 until numberOfItems).map(Index(_)).foldLeft(this) {
        (acc1, itemIndex) =>
          val numberOfDocuments = acc1.getArraySize(DocumentsSection(itemIndex))
          (numberOfDocuments to 1 by -1).map(_ - 1).map(Index(_)).foldLeft(acc1) {
            (acc2, documentIndex) =>
              acc2.get(DocumentPage(itemIndex, documentIndex)) match {
                case Some(itemDocumentUuid) if documentUuid == itemDocumentUuid => acc2.remove(DocumentSection(itemIndex, documentIndex)).getOrElse(acc2)
                case _                                                          => acc2
              }
          }
      }
    case None =>
      this
  }
}

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[UserAnswers] =
    (
      (__ \ "lrn").read[LocalReferenceNumber] and
        (__ \ "eoriNumber").read[EoriNumber] and
        (__ \ "data").read[JsObject] and
        (__ \ "tasks").read[Map[String, TaskStatus]]
    )(UserAnswers.apply _)

  implicit lazy val writes: Writes[UserAnswers] =
    (
      (__ \ "lrn").write[LocalReferenceNumber] and
        (__ \ "eoriNumber").write[EoriNumber] and
        (__ \ "data").write[JsObject] and
        (__ \ "tasks").write[Map[String, TaskStatus]]
    )(unlift(UserAnswers.unapply))
}
