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

package models

import config.{FrontendAppConfig, PhaseConfig}
import pages.document._
import pages.sections.DocumentsSection

case class ConsignmentLevelDocuments(
  previous: Int,
  supporting: Int,
  transport: Int
) {

  def canAdd(documentType: DocumentType)(implicit config: FrontendAppConfig, phaseConfig: PhaseConfig): Boolean = documentType match {
    case DocumentType.Previous  => previous < phaseConfig.maxPreviousDocuments
    case DocumentType.Support   => supporting < config.maxSupportingDocuments
    case DocumentType.Transport => transport < config.maxTransportDocuments
  }

  def cannotAddAnyMore(implicit config: FrontendAppConfig, phaseConfig: PhaseConfig): Boolean =
    !canAdd(DocumentType.Previous) && !canAdd(DocumentType.Support) && !canAdd(DocumentType.Transport)
}

object ConsignmentLevelDocuments {

  def apply(): ConsignmentLevelDocuments = ConsignmentLevelDocuments(0, 0, 0)

  private def apply(values: (Int, Int, Int)): ConsignmentLevelDocuments = ConsignmentLevelDocuments(values._1, values._2, values._3)

  def apply(userAnswers: UserAnswers, documentIndex: Index): ConsignmentLevelDocuments =
    ConsignmentLevelDocuments(userAnswers, Some(documentIndex))

  def apply(userAnswers: UserAnswers, documentIndex: Option[Index] = None): ConsignmentLevelDocuments = {
    val numberOfDocuments = userAnswers.getArraySize(DocumentsSection)

    (0 until numberOfDocuments).map(Index(_)).foldLeft(ConsignmentLevelDocuments()) {
      case (ConsignmentLevelDocuments(previous, supporting, transport), index) if !documentIndex.contains(index) =>
        lazy val documentType = (userAnswers.get(TypePage(index)) orElse userAnswers.get(PreviousDocumentTypePage(index))).map(_.`type`)
        val values = userAnswers.get(AttachToAllItemsPage(index)) match {
          case Some(true) if documentType.contains(DocumentType.Previous)  => (previous + 1, supporting, transport)
          case Some(true) if documentType.contains(DocumentType.Support)   => (previous, supporting + 1, transport)
          case Some(true) if documentType.contains(DocumentType.Transport) => (previous, supporting, transport + 1)
          case _                                                           => (previous, supporting, transport)
        }
        ConsignmentLevelDocuments(values)
      case (consignmentLevelDocuments, _) => consignmentLevelDocuments
    }
  }
}
