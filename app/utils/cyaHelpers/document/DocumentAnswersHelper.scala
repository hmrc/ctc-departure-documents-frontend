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
import models.reference._
import models.{Index, Mode, UserAnswers}
import pages.document._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.cyaHelpers.AnswersHelper

class DocumentAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode,
  documentIndex: Index
)(implicit messages: Messages, config: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

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

  def goodsItemNumberYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddGoodsItemNumberYesNoPage(documentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "document.addGoodsItemNumberYesNo",
    id = Some("change-add-goods-item-number")
  )

  def goodsItemNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = GoodsItemNumberPage(documentIndex),
    formatAnswer = formatAsText,
    prefix = "document.goodsItemNumber",
    id = Some("change-reference-number")
  )

  def typeOfPackageYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddTypeOfPackageYesNoPage(documentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "document.addTypeOfPackageYesNo",
    id = Some("change-add-type-of-package")
  )

  def packageType: Option[SummaryListRow] = getAnswerAndBuildRow[PackageType](
    page = PackageTypePage(documentIndex),
    formatAnswer = formatAsText,
    prefix = "document.packageType",
    id = Some("change-package-type")
  )

  def numberOfPackagesYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddNumberOfPackagesYesNoPage(documentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "document.addNumberOfPackagesYesNo",
    id = Some("change-add-number-of-packages")
  )

  def numberOfPackage: Option[SummaryListRow] = getAnswerAndBuildRow[Int](
    page = NumberOfPackagesPage(documentIndex),
    formatAnswer = formatAsText,
    prefix = "document.numberOfPackages",
    id = Some("change-number-of-packages")
  )

  def declareQuantityOfGoodsYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = DeclareQuantityOfGoodsYesNoPage(documentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "document.declareQuantityOfGoodsYesNo",
    id = Some("change-declare-quantity-of-goods")
  )

  def metric: Option[SummaryListRow] = getAnswerAndBuildRow[Metric](
    page = MetricPage(documentIndex),
    formatAnswer = formatAsText,
    prefix = "document.metric",
    id = Some("change-metric")
  )

  def quantity: Option[SummaryListRow] =
    userAnswers.get(MetricPage(documentIndex)).flatMap {
      metric =>
        getAnswerAndBuildRow[BigDecimal](
          page = QuantityPage(documentIndex),
          formatAnswer = formatAsText,
          prefix = "document.quantity",
          id = Some("change-quantity-of-the-goods"),
          args = metric
        )
    }

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
}
