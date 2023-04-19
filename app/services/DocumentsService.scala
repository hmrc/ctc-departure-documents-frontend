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

package services

import connectors.ReferenceDataConnector
import models.SelectableList
import models.reference.Document
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DocumentsService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getDocuments()(implicit hc: HeaderCarrier): Future[SelectableList[Document]] =
    for {
      documents         <- referenceDataConnector.getDocuments()
      previousDocuments <- referenceDataConnector.getPreviousDocuments()
    } yield sort(documents ++ previousDocuments)

  def getPreviousDocuments()(implicit hc: HeaderCarrier): Future[SelectableList[Document]] =
    referenceDataConnector
      .getPreviousDocuments()
      .map(sort)

  private def sort(documents: Seq[Document]): SelectableList[Document] =
    SelectableList(documents.sortBy(_.description.map(_.toLowerCase)))
}
