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

package utils.cyaHelpers

import controllers.document.routes
import models.journeyDomain.DocumentDomain
import models.{NormalMode, UserAnswers}
import pages.document.{PreviousDocumentTypePage, TypePage}
import pages.sections.DocumentsSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.ListItem

class DocumentsAnswersHelper(
  userAnswers: UserAnswers
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers, NormalMode) {

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(DocumentsSection) {
      documentIndex =>
        DocumentDomain.isMandatoryPrevious(documentIndex).run(userAnswers).toOption.flatMap {
          isMandatoryPrevious =>
            val removeRoute: Option[Call] = if (isMandatoryPrevious) {
              None
            } else {
              Some(routes.RemoveDocumentController.onPageLoad(lrn, documentIndex))
            }

            buildListItem[DocumentDomain](
              nameWhenComplete = _.label,
              nameWhenInProgress = (
                userAnswers.get(TypePage(documentIndex)) orElse userAnswers.get(PreviousDocumentTypePage(documentIndex))
              ).map(_.toString) orElse Some(""),
              removeRoute = removeRoute
            )(DocumentDomain.userAnswersReader(documentIndex))
        }
    }
}
