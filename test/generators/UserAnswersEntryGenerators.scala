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

package generators

import models.reference.{CustomsOffice, Document}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.libs.json._
import queries.Gettable

trait UserAnswersEntryGenerators {
  self: Generators =>

  def generateAnswer: PartialFunction[Gettable[?], Gen[JsValue]] =
    generateExternalAnswer orElse
      generateDocumentsAnswer orElse
      generateDocumentAnswer

  private def generateExternalAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.external._
    {
      case TransitOperationOfficeOfDeparturePage => arbitrary[CustomsOffice].map(Json.toJson(_))
      case TransitOperationDeclarationTypePage   => arbitrary[String](arbitraryDeclarationType).map(Json.toJson(_))
    }
  }

  private def generateDocumentsAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages._
    {
      case AddDocumentsYesNoPage => arbitrary[Boolean].map(JsBoolean)
    }
  }

  private def generateDocumentAnswer: PartialFunction[Gettable[?], Gen[JsValue]] = {
    import pages.document._
    {
      case PreviousDocumentTypePage(_)          => arbitrary[Document].map(Json.toJson(_))
      case TypePage(_)                          => arbitrary[Document].map(Json.toJson(_))
      case AttachToAllItemsPage(_)              => arbitrary[Boolean].map(JsBoolean)
      case DocumentReferenceNumberPage(_)       => nonEmptyString.map(JsString.apply)
      case AddLineItemNumberYesNoPage(_)        => arbitrary[Boolean].map(JsBoolean)
      case LineItemNumberPage(_)                => positiveInts.map(Json.toJson(_))
      case AddAdditionalInformationYesNoPage(_) => arbitrary[Boolean].map(JsBoolean)
      case AdditionalInformationPage(_)         => nonEmptyString.map(JsString.apply)
    }
  }
}
