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

package services

import connectors.ReferenceDataConnector
import models.SelectableList
import models.reference.Document
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

sealed trait DocumentsService {

  val referenceDataConnector: ReferenceDataConnector

  implicit val ec: ExecutionContext

  def getTransportDocuments(attachToAllItems: Boolean)(implicit hc: HeaderCarrier): Future[Seq[Document]] =
    referenceDataConnector.getTransportDocuments()

  def getDocuments(attachToAllItems: Boolean)(implicit hc: HeaderCarrier): Future[SelectableList[Document]] =
    for {
      supportingDocuments <- referenceDataConnector.getSupportingDocuments()
      transportDocuments  <- getTransportDocuments(attachToAllItems)
      previousDocuments   <- referenceDataConnector.getPreviousDocuments()
    } yield sort(supportingDocuments ++ transportDocuments ++ previousDocuments)

  def getPreviousDocuments()(implicit hc: HeaderCarrier): Future[SelectableList[Document]] =
    referenceDataConnector
      .getPreviousDocuments()
      .map(sort)

  private def sort(documents: Seq[Document]): SelectableList[Document] =
    SelectableList(documents.sortBy(_.description.map(_.toLowerCase)))
}

class TransitionDocumentsService @Inject() (
  override val referenceDataConnector: ReferenceDataConnector
)(implicit override val ec: ExecutionContext)
    extends DocumentsService {

  override def getTransportDocuments(attachToAllItems: Boolean)(implicit hc: HeaderCarrier): Future[Seq[Document]] =
    super.getTransportDocuments(attachToAllItems)
}

class PostTransitionDocumentsService @Inject() (
  override val referenceDataConnector: ReferenceDataConnector
)(implicit override val ec: ExecutionContext)
    extends DocumentsService {

  override def getTransportDocuments(attachToAllItems: Boolean)(implicit hc: HeaderCarrier): Future[Seq[Document]] =
    if (attachToAllItems) super.getTransportDocuments(attachToAllItems) else Future.successful(Seq.empty)
}
