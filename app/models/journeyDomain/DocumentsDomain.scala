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

import config.Constants.DeclarationType.{T2, T2F}
import models.{Index, RichJsArray}
import pages.AddDocumentsYesNoPage
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}
import pages.sections.{DocumentsSection, Section}

case class DocumentsDomain(documents: Seq[DocumentDomain]) extends JourneyDomainModel {

  override def page: Option[Section[?]] = Some(DocumentsSection)
}

object DocumentsDomain {

  def isMandatoryPrevious: Read[Boolean] =
    (
      TransitOperationOfficeOfDeparturePage.reader,
      TransitOperationDeclarationTypePage.reader
    ).to {
      case (officeOfDeparture, declarationType) =>
        Read.apply(officeOfDeparture.isInGB && Seq(T2, T2F).contains(declarationType))
    }

  implicit def userAnswersReader: UserAnswersReader[DocumentsDomain] = {
    def arrayReader: Read[Seq[DocumentDomain]] =
      DocumentsSection.arrayReader.to {
        case x if x.isEmpty =>
          DocumentDomain.userAnswersReader(Index(0)).toSeq
        case x =>
          x.traverse[DocumentDomain](DocumentDomain.userAnswersReader(_).apply(_))
      }

    implicit val documentsReader: Read[Seq[DocumentDomain]] =
      isMandatoryPrevious.to {
        case true =>
          arrayReader
        case false =>
          AddDocumentsYesNoPage.reader.to {
            case true  => arrayReader
            case false => UserAnswersReader.emptyList
          }
      }

    documentsReader.map(DocumentsDomain.apply).apply(Nil)
  }
}
