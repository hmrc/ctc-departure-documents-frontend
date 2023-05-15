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

package pages.document

import models.{Index, UserAnswers}
import models.reference.Document
import pages.QuestionPage
import pages.sections.DocumentDetailsSection

import scala.util.Try

trait DocumentTypePage extends QuestionPage[Document] {

  val documentIndex: Index

  override def cleanup(value: Option[Document], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) =>
        userAnswers
          .removeDocumentFromItems(userAnswers.get(DocumentUuidPage(documentIndex)))
          .remove(DocumentDetailsSection(documentIndex))
      case None =>
        super.cleanup(value, userAnswers)
    }

}
