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

package utils.cyaHelpers.document

import config.FrontendAppConfig
import models.journeyDomain.DocumentDomain
import models.{Mode, UserAnswers}
import pages.document.{PreviousDocumentTypePage, TypePage}
import pages.sections.DocumentsSection
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.cyaHelpers.AnswersHelper
import viewModels.ListItem

class DocumentsAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode
)(implicit messages: Messages, config: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(DocumentsSection) {
      documentIndex =>
        buildListItem[DocumentDomain](
          nameWhenComplete = _.asString,
          nameWhenInProgress = (userAnswers.get(TypePage(documentIndex)) orElse userAnswers.get(PreviousDocumentTypePage(documentIndex))).map(_.toString),
          removeRoute = Some(Call("GET", "#")) //TODO: Update to be remove route when built
        )(DocumentDomain.userAnswersReader(documentIndex))
    }
}
