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

import models.{Index, Mode, RichJsArray, UserAnswers}
import pages.sections.DocumentsSection
import play.api.mvc.Call

case class DocumentsDomain(document: Seq[DocumentDomain]) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] = Some(
    controllers.routes.AddAnotherDocumentController.onPageLoad(userAnswers.lrn)
  )
}

object DocumentsDomain {

  implicit def userAnswersReader: UserAnswersReader[DocumentsDomain] = {
    val documentReader: UserAnswersReader[Seq[DocumentDomain]] =
      DocumentsSection.arrayReader.flatMap {
        case x if x.isEmpty =>
          UserAnswersReader[DocumentDomain](
            DocumentDomain.userAnswersReader(Index(0))
          ).map(Seq(_))

        case x =>
          x.traverse[DocumentDomain](
            DocumentDomain.userAnswersReader
          ).map(_.toSeq)
      }

    UserAnswersReader[Seq[DocumentDomain]](documentReader).map(DocumentsDomain(_))
  }
}
