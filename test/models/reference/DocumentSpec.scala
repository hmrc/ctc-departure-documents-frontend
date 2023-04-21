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

package models.reference

import base.SpecBase
import generators.Generators
import models.DocumentType
import models.DocumentType._
import models.reference.Document.referenceDataReads
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import services.UUIDService
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

import java.util.UUID

class DocumentSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val uuid = "8e5a3f69-7d6d-490a-8071-002b1d35d3c1"

  "referenceDataReads" - {

    implicit val uuidService: UUIDService = injector.instanceOf[UUIDService]

    "must deserialise json from reference data service" - {
      "when transport" in {
        val json = Json.parse("""
            |{
            |  "code" : "code",
            |  "description" : "description",
            |  "transportDocument" : true
            |}
            |""".stripMargin)

        val result = json.as[Document](referenceDataReads)
        result.`type` mustBe Transport
        result.code mustBe "code"
        result.description mustBe Some("description")
      }

      "when support" in {
        val json = Json.parse("""
            |{
            |  "code" : "code",
            |  "description" : "description",
            |  "transportDocument" : false
            |}
            |""".stripMargin)

        val result = json.as[Document](referenceDataReads)
        result.`type` mustBe Support
        result.code mustBe "code"
        result.description mustBe Some("description")
      }

      "when previous" in {
        val json = Json.parse("""
            |{
            |  "code" : "code",
            |  "description" : "description"
            |}
            |""".stripMargin)

        val result = json.as[Document](referenceDataReads)
        result.`type` mustBe Previous
        result.code mustBe "code"
        result.description mustBe Some("description")
      }
    }
  }

  "format" - {
    "must deserialise json from mongo" - {
      "when transport" in {
        val json = Json.parse(s"""
            |{
            |  "type" : "Transport",
            |  "code" : "code",
            |  "description" : "description",
            |  "uuid" : "$uuid"
            |}
            |""".stripMargin)

        json.as[Document] mustBe Document(Transport, "code", Some("description"), UUID.fromString(uuid))
      }

      "when support" in {
        val json = Json.parse(s"""
            |{
            |  "type" : "Support",
            |  "code" : "code",
            |  "description" : "description",
            |  "uuid" : "$uuid"
            |}
            |""".stripMargin)

        json.as[Document] mustBe Document(Support, "code", Some("description"), UUID.fromString(uuid))
      }

      "when previous" in {
        val json = Json.parse(s"""
            |{
            |  "type" : "Previous",
            |  "code" : "code",
            |  "description" : "description",
            |  "uuid" : "$uuid"
            |}
            |""".stripMargin)

        json.as[Document] mustBe Document(Previous, "code", Some("description"), UUID.fromString(uuid))
      }
    }

    "must serialise document to json" - {
      "when transport" in {
        val json = Json.parse(s"""
            |{
            |  "type" : "Transport",
            |  "code" : "code",
            |  "description" : "description",
            |  "uuid" : "$uuid"
            |}
            |""".stripMargin)

        Json.toJson(Document(Transport, "code", Some("description"), UUID.fromString(uuid))) mustBe json
      }

      "when support" in {
        val json = Json.parse(s"""
            |{
            |  "type" : "Support",
            |  "code" : "code",
            |  "description" : "description",
            |  "uuid" : "$uuid"
            |}
            |""".stripMargin)

        Json.toJson(Document(Support, "code", Some("description"), UUID.fromString(uuid))) mustBe json
      }

      "when previous" in {
        val json = Json.parse(s"""
            |{
            |  "type" : "Previous",
            |  "code" : "code",
            |  "description" : "description",
            |  "uuid" : "$uuid"
            |}
            |""".stripMargin)

        Json.toJson(Document(Previous, "code", Some("description"), UUID.fromString(uuid))) mustBe json
      }
    }
  }

  "must convert to select item" in {
    forAll(arbitrary[DocumentType], Gen.alphaNumStr, Gen.option(Gen.alphaNumStr), arbitrary[Boolean]) {
      (`type`, code, description, selected) =>
        val document = Document(`type`, code, description, UUID.randomUUID())
        document.toSelectItem(selected) mustBe SelectItem(Some(code), document.toString, selected)
    }
  }

  "must format as string" - {
    "when description defined and non-empty" in {
      forAll(arbitrary[DocumentType], Gen.alphaNumStr, nonEmptyString) {
        (`type`, code, description) =>
          val document = Document(`type`, code, Some(description), UUID.randomUUID())
          document.toString mustBe s"($code) $description"
      }
    }

    "when description defined and empty" in {
      forAll(arbitrary[DocumentType], Gen.alphaNumStr) {
        (`type`, code) =>
          val document = Document(`type`, code, Some(""), UUID.randomUUID())
          document.toString mustBe code
      }
    }

    "when description undefined" in {
      forAll(arbitrary[DocumentType], Gen.alphaNumStr) {
        (`type`, code) =>
          val document = Document(`type`, code, None, UUID.randomUUID())
          document.toString mustBe code
      }
    }
  }
}
