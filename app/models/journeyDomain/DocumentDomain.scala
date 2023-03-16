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

package models.journeyDomain

import cats.implicits._
import models.DeclarationType._
import models.DocumentType._
import models.Index
import models.reference.Document
import pages.document._
import pages.external._

sealed trait DocumentDomain extends JourneyDomainModel {
  val index: Index
  val document: Document
  val referenceNumber: String
}

object DocumentDomain {

  implicit def userAnswersReader(documentIndex: Index): UserAnswersReader[DocumentDomain] =
    (
      TransitOperationOfficeOfDeparturePage.reader,
      TransitOperationDeclarationTypePage.reader
    ).flatMapN {
      case (customsOffice, T2 | T2F) if documentIndex.isFirst && customsOffice.isInGB =>
        PreviousDocumentTypePage(documentIndex).reader
          .flatMap(PreviousDocumentDomain.userAnswersReader(documentIndex, _).widen[DocumentDomain])
      case _ =>
        TypePage(documentIndex).reader.flatMap {
          document =>
            document.`type` match {
              case Support   => SupportDocumentDomain.userAnswersReader(documentIndex, document).widen[DocumentDomain]
              case Transport => TransportDocumentDomain.userAnswersReader(documentIndex, document).widen[DocumentDomain]
              case Previous  => PreviousDocumentDomain.userAnswersReader(documentIndex, document).widen[DocumentDomain]
            }
        }
    }
}

case class SupportDocumentDomain(
  document: Document,
  referenceNumber: String
)(override val index: Index)
    extends DocumentDomain

object SupportDocumentDomain {

  implicit def userAnswersReader(index: Index, document: Document): UserAnswersReader[SupportDocumentDomain] =
    (
      UserAnswersReader(document),
      DocumentReferenceNumberPage(index).reader
    ).tupled.map((SupportDocumentDomain.apply _).tupled).map(_(index))
}

case class TransportDocumentDomain(
  document: Document,
  referenceNumber: String
)(override val index: Index)
    extends DocumentDomain

object TransportDocumentDomain {

  implicit def userAnswersReader(index: Index, document: Document): UserAnswersReader[TransportDocumentDomain] =
    (
      UserAnswersReader(document),
      DocumentReferenceNumberPage(index).reader
    ).tupled.map((TransportDocumentDomain.apply _).tupled).map(_(index))
}

case class PreviousDocumentDomain(
  document: Document,
  referenceNumber: String,
  goodsItemNumber: Option[String]
)(override val index: Index)
    extends DocumentDomain

object PreviousDocumentDomain {

  implicit def userAnswersReader(index: Index, document: Document): UserAnswersReader[PreviousDocumentDomain] =
    (
      UserAnswersReader(document),
      DocumentReferenceNumberPage(index).reader,
      AddGoodsItemNumberYesNoPage(index).filterOptionalDependent(identity)(GoodsItemNumberPage(index).reader)
    ).tupled.map((PreviousDocumentDomain.apply _).tupled).map(_(index))
}
