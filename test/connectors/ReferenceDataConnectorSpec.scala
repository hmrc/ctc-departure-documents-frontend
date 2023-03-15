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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import models.Foo._
import models.reference.Document
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends SpecBase with AppWithDefaultMockFixtures with WireMockServerHandler with ScalaCheckPropertyChecks {

  private val baseUrl = "test-only/transit-movements-trader-reference-data"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder = super
    .guiceApplicationBuilder()
    .configure(
      conf = "microservice.services.referenceData.port" -> server.port()
    )

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  private val previousDocumentJson: String =
    """
      |[
      |  {
      |    "code": "1",
      |    "description": "Certificate of quality"
      |  },
      |  {
      |    "code": "4"
      |  }
      |]
      |""".stripMargin

  private val documentJson: String =
    """
      |[
      | {
      |    "code": "18",
      |    "transportDocument": false,
      |    "description": "Movement certificate A.TR.1"
      |  },
      |  {
      |    "code": "2",
      |    "transportDocument": true,
      |    "description": "Certificate of conformity"
      |  }
      |]
      |""".stripMargin

  "getPreviousDocuments" - {

    "must return list of previous documents when successful" in {
      server.stubFor(
        get(urlEqualTo(s"/$baseUrl/previous-document-types"))
          .willReturn(okJson(previousDocumentJson))
      )

      val expectResult = Seq(
        Document(Previous, "1", Some("Certificate of quality")),
        Document(Previous, "4", None)
      )

      connector.getPreviousDocuments().futureValue mustEqual expectResult
    }

    "must return an exception when an error response is returned" in {

      checkErrorResponse(s"/$baseUrl/previous-document-types", connector.getPreviousDocuments())
    }

  }

  "getDocuments" - {

    "must return list of documents when successful" in {
      server.stubFor(
        get(urlEqualTo(s"/$baseUrl/document-types"))
          .willReturn(okJson(documentJson))
      )

      val expectResult = Seq(
        Document(Support, "18", Some("Movement certificate A.TR.1")),
        Document(Transport, "2", Some("Certificate of conformity"))
      )

      connector.getDocuments().futureValue mustEqual expectResult
    }

    "must return an exception when an error response is returned" in {

      checkErrorResponse(s"/$baseUrl/document-type", connector.getDocuments())
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
