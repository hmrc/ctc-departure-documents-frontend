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

package models.journeyDomain

import cats.implicits._
import controllers.document.routes
import models.DocumentType._
import models.reference.Document
import models.{Index, Mode, UserAnswers}
import pages.document._
import play.api.i18n.Messages
import play.api.mvc.Call

sealed trait DocumentDomain extends JourneyDomainModel {
  val index: Index
  val attachToAllItems: Boolean
  val document: Document
  val referenceNumber: String

  def label(implicit messages: Messages): String = messages("document.label", document, referenceNumber)

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] = Some(
    routes.DocumentAnswersController.onPageLoad(userAnswers.lrn, index)
  )
}

object DocumentDomain {

  def isMandatoryPrevious(documentIndex: Index): UserAnswersReader[Boolean] =
    DocumentsDomain.isMandatoryPrevious.map(_ && documentIndex.isFirst)

  implicit def userAnswersReader(documentIndex: Index): UserAnswersReader[DocumentDomain] =
    (
      isMandatoryPrevious(documentIndex),
      InferredAttachToAllItemsPage(documentIndex).reader orElse AttachToAllItemsPage(documentIndex).reader
    ).flatMapN {
      case (true, attachToAllItems) =>
        PreviousDocumentTypePage(documentIndex).reader
          .flatMap(PreviousDocumentDomain.userAnswersReader(documentIndex, attachToAllItems, _).widen[DocumentDomain])
      case (false, attachToAllItems) =>
        TypePage(documentIndex).reader.flatMap {
          document =>
            document.`type` match {
              case Support   => SupportDocumentDomain.userAnswersReader(documentIndex, attachToAllItems, document).widen[DocumentDomain]
              case Transport => TransportDocumentDomain.userAnswersReader(documentIndex, attachToAllItems, document).widen[DocumentDomain]
              case Previous  => PreviousDocumentDomain.userAnswersReader(documentIndex, attachToAllItems, document).widen[DocumentDomain]
            }
        }
    }
}

case class SupportDocumentDomain(
  attachToAllItems: Boolean,
  document: Document,
  referenceNumber: String,
  lineItemNumber: Option[Int],
  additionalInformation: Option[String]
)(override val index: Index)
    extends DocumentDomain

object SupportDocumentDomain {

  implicit def userAnswersReader(index: Index, attachToAllItems: Boolean, document: Document): UserAnswersReader[SupportDocumentDomain] =
    (
      UserAnswersReader(attachToAllItems),
      UserAnswersReader(document),
      DocumentReferenceNumberPage(index).reader,
      AddLineItemNumberYesNoPage(index).filterOptionalDependent(identity)(LineItemNumberPage(index).reader),
      AddAdditionalInformationYesNoPage(index).filterOptionalDependent(identity)(AdditionalInformationPage(index).reader)
    ).tupled.map((SupportDocumentDomain.apply _).tupled).map(_(index))
}

case class TransportDocumentDomain(
  attachToAllItems: Boolean,
  document: Document,
  referenceNumber: String
)(override val index: Index)
    extends DocumentDomain

object TransportDocumentDomain {

  implicit def userAnswersReader(index: Index, attachToAllItems: Boolean, document: Document): UserAnswersReader[TransportDocumentDomain] =
    (
      UserAnswersReader(attachToAllItems),
      UserAnswersReader(document),
      DocumentReferenceNumberPage(index).reader
    ).tupled.map((TransportDocumentDomain.apply _).tupled).map(_(index))
}

sealed trait PreviousDocumentDomain extends DocumentDomain

object PreviousDocumentDomain {

  implicit def userAnswersReader(index: Index, attachToAllItems: Boolean, document: Document): UserAnswersReader[PreviousDocumentDomain] =
    if (attachToAllItems) {
      PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document).widen[PreviousDocumentDomain]
    } else {
      PreviousDocumentItemLevelDomain.userAnswersReader(index, document).widen[PreviousDocumentDomain]
    }
}

case class PreviousDocumentItemLevelDomain(
  document: Document,
  referenceNumber: String,
  `package`: Option[PackageDomain],
  quantity: Option[QuantityDomain],
  additionalInformation: Option[String]
)(override val index: Index)
    extends PreviousDocumentDomain {

  override val attachToAllItems: Boolean = false
}

object PreviousDocumentItemLevelDomain {

  implicit def userAnswersReader(index: Index, document: Document): UserAnswersReader[PreviousDocumentItemLevelDomain] =
    (
      UserAnswersReader(document),
      DocumentReferenceNumberPage(index).reader,
      AddTypeOfPackageYesNoPage(index).filterOptionalDependent(identity)(PackageDomain.userAnswersReader(index)),
      DeclareQuantityOfGoodsYesNoPage(index).filterOptionalDependent(identity)(QuantityDomain.userAnswersReader(index)),
      AddAdditionalInformationYesNoPage(index).filterOptionalDependent(identity)(AdditionalInformationPage(index).reader)
    ).tupled.map((PreviousDocumentItemLevelDomain.apply _).tupled).map(_(index))
}

case class PreviousDocumentConsignmentLevelDomain(
  document: Document,
  referenceNumber: String,
  additionalInformation: Option[String]
)(override val index: Index)
    extends PreviousDocumentDomain {

  override val attachToAllItems: Boolean = true
}

object PreviousDocumentConsignmentLevelDomain {

  implicit def userAnswersReader(index: Index, document: Document): UserAnswersReader[PreviousDocumentConsignmentLevelDomain] =
    (
      UserAnswersReader(document),
      DocumentReferenceNumberPage(index).reader,
      AddAdditionalInformationYesNoPage(index).filterOptionalDependent(identity)(AdditionalInformationPage(index).reader)
    ).tupled.map((PreviousDocumentConsignmentLevelDomain.apply _).tupled).map(_(index))
}
