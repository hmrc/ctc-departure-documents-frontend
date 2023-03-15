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

package models

import base.SpecBase
import generators.Generators
import models.DocumentType._
import models.reference.Document
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DocumentListSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "getDocument" - {
    "return a document if it exists" in {
      forAll(nonEmptyListOf[Document](10)) {
        documents =>
          val documentTypeList = DocumentList(documents.toList)

          val documentTypeCode = documents.head.code

          documentTypeList.getDocument(documentTypeCode).value mustEqual documents.head
      }
    }

    "return a None if it does not exists" in {
      val documentTypes = Seq(
        Document(Transport, "01", Some("documentType1")),
        Document(Support, "02", Some("documentType2")),
        Document(Previous, "03", Some("documentType3"))
      )

      val documentTypeList = DocumentList(documentTypes)

      val documentTypeCode = "04"

      documentTypeList.getDocument(documentTypeCode) mustBe None
    }
  }
}
