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

import cats.data.NonEmptySet
import com.github.tomakehurst.wiremock.client.WireMock.*
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import itbase.{ItSpecBase, WireMockServerHandler}
import models.DocumentType.*
import models.reference.{Document, Metric, PackageType}
import org.scalacheck.Gen
import org.scalatest.{Assertion, EitherValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsResultException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with EitherValues {

  private val baseUrl = "customs-reference-data/test-only"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(
      conf = "microservice.services.customs-reference-data.port" -> server.port()
    )

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  private def documentsJson(docType: String): String =
    s"""
      |{
      |"_links": {
      |    "self": {
      |      "href": "/customs-reference-data/lists/$docType"
      |    }
      |  },
      |  "meta": {
      |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
      |    "snapshotDate": "2023-01-01"
      |  },
      |  "id": "$docType",
      |  "data": [
      |    {
      |      "activeFrom": "2023-01-23",
      |      "state": "valid",
      |      "code": "1",
      |      "description": "Certificate of quality"
      |    },
      |    {
      |      "code": "4",
      |      "description": "Blah"
      |    }
      |  ]
      |}
      |""".stripMargin

  "getPreviousDocuments" - {
    val url = s"/$baseUrl/lists/PreviousDocumentType"

    "must return list of previous documents when successful" in {
      server.stubFor(
        get(urlPathMatching(url))
          .willReturn(okJson(documentsJson("PreviousDocumentType")))
      )

      val expectResult = NonEmptySet.of(
        Document(Previous, "1", "Certificate of quality"),
        Document(Previous, "4", "Blah")
      )

      connector.getPreviousDocuments().futureValue.value mustEqual expectResult
    }

    "must throw a NoReferenceDataFoundException for an empty response" in {
      checkNoReferenceDataFoundResponse(url, connector.getPreviousDocuments())
    }

    "must return an exception when an error response is returned" in {
      checkErrorResponse(url, connector.getPreviousDocuments())
    }

    "must return an exception when invalid JSON is returned" in {
      checkJsErrorResponse(url, connector.getPreviousDocuments())
    }
  }

  "getTransportDocuments" - {
    val url = s"/$baseUrl/lists/TransportDocumentType"

    "must return list of documents when successful" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(okJson(documentsJson("TransportDocumentType")))
      )

      val expectResult = NonEmptySet.of(
        Document(Transport, "1", "Certificate of quality"),
        Document(Transport, "4", "Blah")
      )

      connector.getTransportDocuments().futureValue.value mustEqual expectResult
    }

    "must throw a NoReferenceDataFoundException for an empty response" in {
      checkNoReferenceDataFoundResponse(url, connector.getTransportDocuments())
    }

    "must return an exception when an error response is returned" in {
      checkErrorResponse(url, connector.getTransportDocuments())
    }

    "must return an exception when invalid JSON is returned" in {
      checkJsErrorResponse(url, connector.getTransportDocuments())
    }
  }

  "getSupportingDocuments" - {
    val url = s"/$baseUrl/lists/SupportingDocumentType"

    "must return list of documents when successful" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(okJson(documentsJson("SupportingDocumentType")))
      )

      val expectResult = NonEmptySet.of(
        Document(Support, "1", "Certificate of quality"),
        Document(Support, "4", "Blah")
      )

      connector.getSupportingDocuments().futureValue.value mustEqual expectResult
    }

    "must throw a NoReferenceDataFoundException for an empty response" in {
      checkNoReferenceDataFoundResponse(url, connector.getSupportingDocuments())
    }

    "must return an exception when an error response is returned" in {
      checkErrorResponse(url, connector.getSupportingDocuments())
    }

    "must return an exception when invalid JSON is returned" in {
      checkJsErrorResponse(url, connector.getSupportingDocuments())
    }
  }

  "getMetrics" - {
    val url = s"/$baseUrl/lists/Unit"

    val json: String =
      """
        |{
        |"_links": {
        |    "self": {
        |      "href": "/customs-reference-data/lists/Unit"
        |    }
        |  },
        |  "meta": {
        |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
        |    "snapshotDate": "2023-01-01"
        |  },
        |  "id": "Unit",
        |  "data": [
        |    {
        |      "activeFrom": "2023-01-23",
        |      "state": "valid",
        |      "code": "CTM",
        |      "description": "Carats (one metric carat = 2 x 10-4kg)"
        |    },
        |    {
        |      "activeFrom": "2023-01-23",
        |      "state": "valid",
        |      "code": "DTN",
        |      "description": "Hectokilogram"
        |    }
        |]
        |}
        |""".stripMargin

    "must return list of metrics when successful" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(okJson(json))
      )

      val expectResult = NonEmptySet.of(
        Metric("CTM", "Carats (one metric carat = 2 x 10-4kg)"),
        Metric("DTN", "Hectokilogram")
      )

      connector.getMetrics().futureValue.value mustEqual expectResult
    }

    "must throw a NoReferenceDataFoundException for an empty response" in {
      checkNoReferenceDataFoundResponse(url, connector.getMetrics())
    }

    "must return an exception when an error response is returned" in {
      checkErrorResponse(url, connector.getMetrics())
    }

    "must return an exception when invalid JSON is returned" in {
      checkJsErrorResponse(url, connector.getMetrics())
    }
  }

  "getPackageTypes" - {
    val url = s"/$baseUrl/lists/KindOfPackages"

    val json: String =
      """
        |{
        |  "_links": {
        |    "self": {
        |      "href": "/customs-reference-data/lists/KindOfPackages"
        |    }
        |  },
        |  "meta": {
        |    "version": "410157ad-bc37-4e71-af2a-404d1ddad94c",
        |    "snapshotDate": "2023-01-01"
        |  },
        |  "id": "KindOfPackages",
        |  "data": [
        |    {
        |      "code": "1A",
        |      "description": "Drum, steel"
        |    },
        |    {
        |      "code": "1B",
        |      "description": "Drum, aluminium"
        |    }
        |  ]
        |}
        |""".stripMargin

    "must return list of package types when successful" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(okJson(json))
      )

      val expectResult = NonEmptySet.of(
        PackageType("1A", "Drum, steel"),
        PackageType("1B", "Drum, aluminium")
      )

      connector.getPackageTypes().futureValue.value mustEqual expectResult
    }

    "must throw a NoReferenceDataFoundException for an empty response" in {
      checkNoReferenceDataFoundResponse(url, connector.getPackageTypes())
    }

    "must return an exception when an error response is returned" in {
      checkErrorResponse(url, connector.getPackageTypes())
    }

    "must return an exception when invalid JSON is returned" in {
      checkJsErrorResponse(url, connector.getPackageTypes())
    }
  }

  private def checkNoReferenceDataFoundResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    val json: String =
      """
        |{
        |  "data": []
        |}
        |""".stripMargin

    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(json))
    )

    result.futureValue.left.value mustBe an[NoReferenceDataFoundException]
  }

  private def checkErrorResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    val errorResponses: Gen[Int] = Gen.chooseNum(400: Int, 599: Int)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        result.futureValue.left.value mustBe an[Exception]
    }
  }

  private def checkJsErrorResponse(url: String, result: => Future[Either[Exception, ?]]): Assertion = {
    val json =
      """
        |{
        |  "foo" : "bar"
        |}
        |""".stripMargin

    server.stubFor(
      get(urlEqualTo(url))
        .willReturn(okJson(json))
    )

    result.futureValue.left.value mustBe a[JsResultException]
  }
}
