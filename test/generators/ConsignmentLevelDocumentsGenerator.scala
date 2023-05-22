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

package generators

import base.SpecBase
import models.reference.{CustomsOffice, Document}
import models.{DeclarationType, Index, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.document.{AttachToAllItemsPage, PreviousDocumentTypePage, TypePage}
import pages.external.{TransitOperationDeclarationTypePage, TransitOperationOfficeOfDeparturePage}

trait ConsignmentLevelDocumentsGenerator {
  self: SpecBase with Generators =>

  private val numberOfPreviousAndSupportingDocuments: Int = frontendAppConfig.maxPreviousDocuments + frontendAppConfig.maxSupportingDocuments

  val numberOfDocuments: Int = numberOfPreviousAndSupportingDocuments + frontendAppConfig.maxTransportDocuments

  def userAnswersWithConsignmentLevelDocumentsMaxedOut: UserAnswers = {
    val initialAnswers = emptyUserAnswers
      .setValue(TransitOperationOfficeOfDeparturePage, arbitrary[CustomsOffice].sample.value)
      .setValue(TransitOperationDeclarationTypePage, arbitrary[DeclarationType].sample.value)

    (0 until numberOfDocuments).foldLeft(initialAnswers) {
      (acc, i) =>
        val ua = acc.setValue(AttachToAllItemsPage(Index(i)), true)
        i match {
          case it if 0 until frontendAppConfig.maxPreviousDocuments contains it =>
            ua
              .setValue(TypePage(Index(i)), arbitrary[Document](arbitraryPreviousDocument).sample.value)
              .setValue(PreviousDocumentTypePage(Index(i)), arbitrary[Document](arbitraryPreviousDocument).sample.value)
          case it if frontendAppConfig.maxPreviousDocuments until numberOfPreviousAndSupportingDocuments contains it =>
            ua.setValue(TypePage(Index(i)), arbitrary[Document](arbitrarySupportDocument).sample.value)
          case it if numberOfPreviousAndSupportingDocuments until numberOfDocuments contains it =>
            ua.setValue(TypePage(Index(i)), arbitrary[Document](arbitraryTransportDocument).sample.value)
        }
    }
  }

}
