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

import cats.Order
import cats.data.NonEmptySet
import config.FrontendAppConfig
import connectors.ReferenceDataConnector.{NoReferenceDataFoundException, Responses}
import models.DocumentType.*
import models.reference.*
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) extends Logging {

  private def get[T](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[Responses[T]]): Future[Responses[T]] =
    http
      .get(url)
      .setHeader(HeaderNames.Accept -> "application/vnd.hmrc.2.0+json")
      .execute[Responses[T]]

  def getPreviousDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Document]] = {
    val url                             = url"${config.referenceDataUrl}/lists/PreviousDocumentType"
    implicit val reads: Reads[Document] = Document.reads(Previous)
    get[Document](url)
  }

  def getSupportingDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Document]] = {
    val url                             = url"${config.referenceDataUrl}/lists/SupportingDocumentType"
    implicit val reads: Reads[Document] = Document.reads(Support)
    get[Document](url)
  }

  def getTransportDocuments()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Document]] = {
    val url                             = url"${config.referenceDataUrl}/lists/TransportDocumentType"
    implicit val reads: Reads[Document] = Document.reads(Transport)
    get[Document](url)
  }

  def getPackageTypes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[PackageType]] = {
    val url = url"${config.referenceDataUrl}/lists/KindOfPackages"
    get[PackageType](url)
  }

  def getMetrics()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Metric]] = {
    val url = url"${config.referenceDataUrl}/lists/Unit"
    get[Metric](url)
  }

  implicit def responseHandlerGeneric[A](implicit reads: Reads[List[A]], order: Order[A]): HttpReads[Responses[A]] =
    (_: String, url: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              Left(NoReferenceDataFoundException(url))
            case JsSuccess(head :: tail, _) =>
              Right(NonEmptySet.of(head, tail*))
            case JsError(errors) =>
              Left(JsResultException(errors))
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          Left(Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}"))
      }
}

object ReferenceDataConnector {

  type Responses[T] = Either[Exception, NonEmptySet[T]]

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
