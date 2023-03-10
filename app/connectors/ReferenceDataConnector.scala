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

package connectors

import config.FrontendAppConfig
import models.reference.{DocumentType, PreviousDocumentType}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient) {

  def getPreviousReferencesDocumentTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[PreviousDocumentType]] = {
    val serviceUrl = s"${config.referenceDataUrl}/previous-document-types"
    http.GET[Seq[PreviousDocumentType]](serviceUrl, headers = version2Header)
  }

  def getDocumentTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[DocumentType]] = {
    val serviceUrl = s"${config.referenceDataUrl}/document-types"
    http.GET[Seq[DocumentType]](serviceUrl, headers = version2Header)
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )
}
