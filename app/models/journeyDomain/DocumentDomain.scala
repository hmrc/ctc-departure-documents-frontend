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
import models.Index
import pages.document.{PreviousDocumentTypePage, TypePage}
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}

case class DocumentDomain() extends JourneyDomainModel

object DocumentDomain {

  implicit def userAnswersReader(documentIndex: Index): UserAnswersReader[DocumentDomain] =
    (
      TransitOperationOfficeOfDeparturePage.reader,
      TransitOperationDeclarationTypePage.reader
    ).flatMapN {
      case (customsOffice, T2 | T2F) if documentIndex.isFirst && customsOffice.isInGB =>
        PreviousDocumentTypePage(documentIndex).reader.map(
          _ => DocumentDomain()
        )
      case _ =>
        TypePage(documentIndex).reader.map(
          _ => DocumentDomain()
        )
    }
}
