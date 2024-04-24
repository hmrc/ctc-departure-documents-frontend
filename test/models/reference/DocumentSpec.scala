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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class DocumentSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "referenceDataReads" - {
    "must deserialise json from reference data service" - {
      "when transport" in {
        val json = Json.parse("""
            |{
            |  "code" : "code",
            |  "description" : "description",
            |  "transportDocument" : true
            |}
            |""".stripMargin)

        json.as[Document](Document.reads(Transport)) mustBe Document(Transport, "code", "description")
      }

      "when support" in {
        val json = Json.parse("""
            |{
            |  "code" : "code",
            |  "description" : "description",
            |  "transportDocument" : false
            |}
            |""".stripMargin)

        json.as[Document](Document.reads(Support)) mustBe Document(Support, "code", "description")
      }

      "when previous" in {
        val json = Json.parse("""
            |{
            |  "code" : "code",
            |  "description" : "description"
            |}
            |""".stripMargin)

        json.as[Document](Document.reads(Previous)) mustBe Document(Previous, "code", "description")
      }
    }
  }

  "format" - {
    "must deserialise json from mongo" - {
      "when transport" in {
        val json = Json.parse("""
            |{
            |  "type" : "Transport",
            |  "code" : "code",
            |  "description" : "description"
            |}
            |""".stripMargin)

        json.as[Document] mustBe Document(Transport, "code", "description")
      }

      "when support" in {
        val json = Json.parse("""
            |{
            |  "type" : "Support",
            |  "code" : "code",
            |  "description" : "description"
            |}
            |""".stripMargin)

        json.as[Document] mustBe Document(Support, "code", "description")
      }

      "when previous" in {
        val json = Json.parse("""
            |{
            |  "type" : "Previous",
            |  "code" : "code",
            |  "description" : "description"
            |}
            |""".stripMargin)

        json.as[Document] mustBe Document(Previous, "code", "description")
      }
    }

    "must serialise document to json" - {
      "when transport" in {
        val json = Json.parse("""
            |{
            |  "type" : "Transport",
            |  "code" : "code",
            |  "description" : "description"
            |}
            |""".stripMargin)

        Json.toJson(Document(Transport, "code", "description")) mustBe json
      }

      "when support" in {
        val json = Json.parse("""
            |{
            |  "type" : "Support",
            |  "code" : "code",
            |  "description" : "description"
            |}
            |""".stripMargin)

        Json.toJson(Document(Support, "code", "description")) mustBe json
      }

      "when previous" in {
        val json = Json.parse("""
            |{
            |  "type" : "Previous",
            |  "code" : "code",
            |  "description" : "description"
            |}
            |""".stripMargin)

        Json.toJson(Document(Previous, "code", "description")) mustBe json
      }
    }
  }

  "must convert to select item" in {
    forAll(arbitrary[DocumentType], Gen.alphaNumStr, Gen.alphaNumStr, arbitrary[Boolean]) {
      (`type`, code, description, selected) =>
        val document = Document(`type`, code, description)
        document.toSelectItem(selected) mustBe SelectItem(Some(document.toString), document.toString, selected)
    }
  }

  "must format as string" in {
    forAll(arbitrary[DocumentType], Gen.alphaNumStr, nonEmptyString) {
      (`type`, code, description) =>
        val document = Document(`type`, code, description)
        document.toString mustBe s"${`type`.display} - ($code) $description"
    }
  }
}
