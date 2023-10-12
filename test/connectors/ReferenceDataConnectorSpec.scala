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

import base.{AppWithDefaultMockFixtures, SpecBase, WireMockServerHandler}
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo, urlPathMatching}
import models.DocumentType._
import models.reference.{Document, Metric}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockServerHandler with ScalaCheckPropertyChecks {

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
      |  {
      |    "activeFrom": "2023-01-23",
      |    "state": "valid",
      |    "code": "1",
      |    "description": "Certificate of quality"
      |  },
      |  {
      |    "code": "4"
      |  }
      |  ]
      |}
      |""".stripMargin

  private val metricJson: String =
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
      |  {
      |    "activeFrom": "2023-01-23",
      |    "state": "valid",
      |    "code": "CTM",
      |    "description": "Carats (one metric carat = 2 x 10-4kg)"
      |  },
      |  {
      |      "activeFrom": "2023-01-23",
      |      "state": "valid",
      |      "code": "DTN",
      |      "description": "Hectokilogram"
      |  }
      |]
      |}
      |""".stripMargin

  "getPreviousDocuments" - {

    val url = s"/$baseUrl/lists/PreviousDocumentType"

    "must return list of previous documents when successful" in {
      server.stubFor(
        get(urlPathMatching(url))
          .willReturn(okJson(documentsJson("PreviousDocumentType")))
      )

      val expectResult = Seq(
        Document(Previous, "1", Some("Certificate of quality")),
        Document(Previous, "4", None)
      )

      connector.getPreviousDocuments().futureValue mustEqual expectResult
    }

    "must return an exception when an error response is returned" in {

      checkErrorResponse(url, connector.getPreviousDocuments())
    }
  }

  "getTransportDocuments" - {

    val url = s"/$baseUrl/lists/TransportDocumentType"

    "must return list of documents when successful" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(okJson(documentsJson("TransportDocumentType")))
      )

      val expectResult = Seq(
        Document(Transport, "1", Some("Certificate of quality")),
        Document(Transport, "4", None)
      )

      connector.getTransportDocuments().futureValue mustEqual expectResult
    }

    "must return an exception when an error response is returned" in {

      checkErrorResponse(url, connector.getTransportDocuments())
    }
  }

  "getSupportingDocuments" - {

    val url = s"/$baseUrl/lists/SupportingDocumentType"

    "must return list of documents when successful" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(okJson(documentsJson("SupportingDocumentType")))
      )

      val expectResult = Seq(
        Document(Support, "1", Some("Certificate of quality")),
        Document(Support, "4", None)
      )

      connector.getSupportingDocuments().futureValue mustEqual expectResult
    }

    "must return an exception when an error response is returned" in {

      checkErrorResponse(url, connector.getSupportingDocuments())
    }
  }

  "getMetrics" - {

    "must return list of metrics when successful" in {
      server.stubFor(
        get(urlEqualTo(s"/$baseUrl/lists/Unit"))
          .willReturn(okJson(metricJson))
      )

      val expectResult = Seq(
        Metric("CTM", "Carats (one metric carat = 2 x 10-4kg)"),
        Metric("DTN", "Hectokilogram")
      )

      connector.getMetrics().futureValue mustEqual expectResult
    }

    "must return an exception when an error response is returned" in {

      checkErrorResponse(s"/$baseUrl/metrics", connector.getMetrics())
    }

  }

  private def checkErrorResponse(url: String, result: => Future[_]): Assertion = {
    val errorResponses: Gen[Int] = Gen
      .chooseNum(400: Int, 599: Int)
      .suchThat(_ != 404)

    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady(result.failed) {
          _ mustBe an[Exception]
        }
    }
  }

}
