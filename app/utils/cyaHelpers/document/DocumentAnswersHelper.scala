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

package utils.cyaHelpers.document

import models.reference._
import models.{CheckMode, Index, UserAnswers}
import pages.document._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.cyaHelpers.AnswersHelper

class DocumentAnswersHelper(
  userAnswers: UserAnswers,
  documentIndex: Index
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers, CheckMode) {

  def attachToAllItems: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AttachToAllItemsPage(documentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "document.attachToAllItems",
    id = Some("change-attach-to-all-items")
  )

  def documentType: Option[SummaryListRow] = getAnswerAndBuildRow[Document](
    page = TypePage(documentIndex),
    formatAnswer = formatAsText,
    prefix = "document.type",
    id = Some("change-type")
  )

  def previousDocumentType: Option[SummaryListRow] = getAnswerAndBuildRow[Document](
    page = PreviousDocumentTypePage(documentIndex),
    formatAnswer = formatAsText,
    prefix = "document.previousDocumentType",
    id = Some("change-previous-document-type")
  )

  def documentReferenceNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = DocumentReferenceNumberPage(documentIndex),
    formatAnswer = formatAsText,
    prefix = "document.documentReferenceNumber",
    id = Some("change-reference-number")
  )

  def lineItemNumberYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddLineItemNumberYesNoPage(documentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "document.addLineItemNumberYesNo",
    id = Some("change-add-line-item-number")
  )

  def lineItemNumber: Option[SummaryListRow] = getAnswerAndBuildRow[Int](
    page = LineItemNumberPage(documentIndex),
    formatAnswer = formatAsText,
    prefix = "document.lineItemNumber",
    id = Some("change-line-item-number")
  )

  def additionalInformationYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddAdditionalInformationYesNoPage(documentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "document.addAdditionalInformationYesNo",
    id = Some("change-add-additional-information")
  )

  def additionalInformation: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AdditionalInformationPage(documentIndex),
    formatAnswer = formatAsText,
    prefix = "document.additionalInformation",
    id = Some("change-additional-information")
  )
}
