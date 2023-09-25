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

import config.Constants.{T2, T2F}
import models.{Index, Mode, RichJsArray, UserAnswers}
import pages.AddDocumentsYesNoPage
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}
import pages.sections.DocumentsSection
import play.api.mvc.Call

case class DocumentsDomain(documents: Seq[DocumentDomain]) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] = Some(
    controllers.routes.AddAnotherDocumentController.onPageLoad(userAnswers.lrn)
  )
}

object DocumentsDomain {

  def isMandatoryPrevious: UserAnswersReader[Boolean] =
    for {
      officeOfDeparture <- TransitOperationOfficeOfDeparturePage.reader
      declarationType   <- TransitOperationDeclarationTypePage.reader
    } yield officeOfDeparture.isInGB && Seq(T2, T2F).contains(declarationType)

  implicit def userAnswersReader: UserAnswersReader[DocumentsDomain] = {
    def arrayReader: UserAnswersReader[Seq[DocumentDomain]] = DocumentsSection.arrayReader.flatMap {
      case x if x.isEmpty =>
        UserAnswersReader[DocumentDomain](
          DocumentDomain.userAnswersReader(Index(0))
        ).map(Seq(_))
      case x =>
        x.traverse[DocumentDomain](
          DocumentDomain.userAnswersReader
        ).map(_.toSeq)
    }

    implicit val documentsReader: UserAnswersReader[Seq[DocumentDomain]] =
      isMandatoryPrevious.flatMap {
        case true => arrayReader
        case false =>
          AddDocumentsYesNoPage.reader.flatMap {
            case true  => arrayReader
            case false => UserAnswersReader(Nil)
          }
      }

    UserAnswersReader[Seq[DocumentDomain]].map(DocumentsDomain(_))
  }
}
