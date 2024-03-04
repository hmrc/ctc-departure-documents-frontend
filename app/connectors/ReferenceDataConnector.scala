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

import cats.Order
import cats.data.NonEmptySet
import config.FrontendAppConfig
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.DocumentType._
import models.reference.{Document, Metric, PackageType}
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) extends Logging {

  def getPreviousDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Document]] = {
    val url                             = url"${config.referenceDataUrl}/lists/PreviousDocumentType"
    implicit val reads: Reads[Document] = Document.reads(Previous)
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Document]]
  }

  def getSupportingDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Document]] = {
    val url                             = url"${config.referenceDataUrl}/lists/SupportingDocumentType"
    implicit val reads: Reads[Document] = Document.reads(Support)
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Document]]
  }

  def getTransportDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Document]] = {
    val url                             = url"${config.referenceDataUrl}/lists/TransportDocumentType"
    implicit val reads: Reads[Document] = Document.reads(Transport)
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Document]]
  }

  def getPackageTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[PackageType]] = {
    val url = url"${config.referenceDataUrl}/lists/KindOfPackages"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[PackageType]]
  }

  def getMetrics()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Metric]] = {
    val url = url"${config.referenceDataUrl}/lists/Unit"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Metric]]
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A], order: Order[A]): HttpReads[NonEmptySet[A]] =
    (_: String, url: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException(url)
            case JsSuccess(head :: tail, _) =>
              NonEmptySet.of(head, tail: _*)
            case JsError(errors) =>
              throw JsResultException(errors)
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          throw new Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}")
      }
    }
}

object ReferenceDataConnector {

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
