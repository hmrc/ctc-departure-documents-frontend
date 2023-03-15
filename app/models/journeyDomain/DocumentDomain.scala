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
import models.DeclarationType.{T2, T2F}
import models.DocumentType._
import models.Index
import models.reference.Document
import pages.document.{PreviousDocumentTypePage, TypePage}
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}

sealed trait DocumentDomain extends JourneyDomainModel {
  val document: Document
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
  document: Document
) extends DocumentDomain

object SupportDocumentDomain {

  implicit def userAnswersReader(index: Index, document: Document): UserAnswersReader[SupportDocumentDomain] =
    UserAnswersReader(SupportDocumentDomain(document))
}

case class TransportDocumentDomain(
  document: Document
) extends DocumentDomain

object TransportDocumentDomain {

  implicit def userAnswersReader(index: Index, document: Document): UserAnswersReader[TransportDocumentDomain] =
    UserAnswersReader(TransportDocumentDomain(document))
}

case class PreviousDocumentDomain(
  document: Document
) extends DocumentDomain

object PreviousDocumentDomain {

  implicit def userAnswersReader(index: Index, document: Document): UserAnswersReader[PreviousDocumentDomain] =
    UserAnswersReader(PreviousDocumentDomain(document))
}
