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

package connectors

import config.FrontendAppConfig
import models.DocumentType._
import models.reference.{Document, Metric, PackageType}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.Reads
import sttp.model.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient) extends Logging {

  def getPreviousDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Document]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/PreviousDocumentType"
    http.GET[Seq[Document]](serviceUrl, headers = version2Header)(Document.httpReads(Previous), hc, ec)
  }

  def getSupportingDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Document]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/SupportingDocumentType"
    http.GET[Seq[Document]](serviceUrl, headers = version2Header)(Document.httpReads(Support), hc, ec)
  }

  def getTransportDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Document]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/TransportDocumentType"
    http.GET[Seq[Document]](serviceUrl, headers = version2Header)(Document.httpReads(Transport), hc, ec)
  }

  def getPackageTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[PackageType]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/KindOfPackages"
    http.GET[Seq[PackageType]](serviceUrl, headers = version2Header)
  }

  def getMetrics()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Metric]] = {
    val serviceUrl = s"${config.referenceDataUrl}/lists/Unit"
    http.GET[Seq[Metric]](serviceUrl, headers = version2Header)
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A]): HttpReads[Seq[A]] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          val referenceData = (response.json \ "data").getOrElse(
            throw new IllegalStateException("[ReferenceDataConnector][responseHandlerGeneric] Reference data could not be parsed")
          )

          referenceData.as[Seq[A]]
        case NO_CONTENT =>
          Nil
        case NOT_FOUND =>
          logger.warn("[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned NOT_FOUND")
          throw new IllegalStateException("[ReferenceDataConnector][responseHandlerGeneric] Reference data could not be found")
        case other =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Invalid downstream status $other")
          throw new IllegalStateException(s"[ReferenceDataConnector][responseHandlerGeneric] Invalid Downstream Status $other")
      }
    }
}
