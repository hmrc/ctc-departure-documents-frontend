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

package viewModels.document

import models.reference.Document
import models.{Index, RichJsArray, UserAnswers}
import pages.document.{DocumentReferenceNumberPage, PreviousDocumentTypePage, TypePage}
import pages.sections.DocumentsSection

import javax.inject.Inject

case class DocumentReferenceNumberViewModel(
  otherReferenceNumbers: Seq[String]
)

object DocumentReferenceNumberViewModel {

  class DocumentReferenceNumberViewModelProvider @Inject() () {

    def apply(userAnswers: UserAnswers, index: Index): DocumentReferenceNumberViewModel =
      DocumentReferenceNumberViewModel.apply(userAnswers, index)
  }

  def apply(userAnswers: UserAnswers, index: Index): DocumentReferenceNumberViewModel = {
    def getDocument(index: Index): Option[Document] = userAnswers.get(TypePage(index)) orElse
      userAnswers.get(PreviousDocumentTypePage(index))

    val otherReferenceNumbers = getDocument(index) match {
      case Some(documentAtIndex) =>
        userAnswers
          .getArray(DocumentsSection)
          .zipWithIndex
          .filterNot(_._2 == index)
          .flatMap {
            case (_, index) =>
              getDocument(index) match {
                case Some(`documentAtIndex`) => userAnswers.get(DocumentReferenceNumberPage(index))
                case _                       => None
              }
          }
      case None =>
        Nil
    }

    new DocumentReferenceNumberViewModel(otherReferenceNumbers)
  }
}
