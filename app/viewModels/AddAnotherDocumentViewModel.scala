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

package viewModels

import config.FrontendAppConfig
import models.{DocumentType, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.cyaHelpers.DocumentsAnswersHelper
import viewModels.Entity.Document

import javax.inject.Inject

case class AddAnotherDocumentViewModel(
  override val listItems: Seq[ListItem[Document]],
  onSubmitCall: Call
) extends AddAnotherViewModel[Document] {
  override val prefix: String = "addAnotherDocument"

  override def allowMore(implicit config: FrontendAppConfig): Boolean = {
    def allowMore(`type`: DocumentType, max: Int): Boolean = {
      val numberOfConsignmentLevelDocuments = listItems.count {
        li => li.entity.attachToAllItems && li.entity.`type`.contains(`type`)
      }
      numberOfConsignmentLevelDocuments < max
    }

    allowMore(DocumentType.Previous, config.maxPreviousDocuments) &&
    allowMore(DocumentType.Support, config.maxSupportingDocuments) &&
    allowMore(DocumentType.Transport, config.maxTransportDocuments)
  }
}

object AddAnotherDocumentViewModel {

  class AddAnotherDocumentViewModelProvider @Inject() () {

    def apply(userAnswers: UserAnswers)(implicit messages: Messages): AddAnotherDocumentViewModel = {
      val helper = new DocumentsAnswersHelper(userAnswers)

      val listItems = helper.listItems.collect {
        case Left(value)  => value
        case Right(value) => value
      }

      new AddAnotherDocumentViewModel(
        listItems,
        onSubmitCall = controllers.routes.AddAnotherDocumentController.onSubmit(userAnswers.lrn)
      )
    }
  }
}
