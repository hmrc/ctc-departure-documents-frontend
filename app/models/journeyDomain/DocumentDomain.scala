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

package models.journeyDomain

import models.DocumentType._
import models.Index
import models.reference.Document
import pages.document._
import pages.sections.{DocumentSection, Section}
import play.api.i18n.Messages

sealed trait DocumentDomain extends JourneyDomainModel {
  val index: Index
  val attachToAllItems: Boolean
  val document: Document
  val referenceNumber: String

  def asString(implicit messages: Messages): String =
    DocumentDomain.asString(index, Some(document), Some(referenceNumber))

  override def page: Option[Section[_]] = Some(DocumentSection(index))
}

object DocumentDomain {

  def asString(index: Index, document: Option[Document], referenceNumber: Option[String])(implicit messages: Messages): String =
    (document, referenceNumber) match {
      case (Some(document), Some(referenceNumber)) => messages("document.label", document.toString, referenceNumber)
      case (Some(document), None)                  => document.toString
      case _                                       => index.display.toString
    }

  def isMandatoryPrevious(documentIndex: Index): Read[Boolean] =
    DocumentsDomain.isMandatoryPrevious.to {
      x => Read.apply(x && documentIndex.isFirst)
    }

  implicit def userAnswersReader(documentIndex: Index): Read[DocumentDomain] =
    (
      isMandatoryPrevious(documentIndex),
      UserAnswersReader.readInferred(AttachToAllItemsPage(documentIndex), InferredAttachToAllItemsPage(documentIndex))
    ).to {
      case (true, attachToAllItems) =>
        PreviousDocumentTypePage(documentIndex).reader.to(PreviousDocumentDomain.userAnswersReader(documentIndex, attachToAllItems, _))
      case (false, attachToAllItems) =>
        TypePage(documentIndex).reader.to {
          document =>
            document.`type` match {
              case Support   => SupportDocumentDomain.userAnswersReader(documentIndex, attachToAllItems, document)
              case Transport => TransportDocumentDomain.userAnswersReader(documentIndex, attachToAllItems, document)
              case Previous  => PreviousDocumentDomain.userAnswersReader(documentIndex, attachToAllItems, document)
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

  implicit def userAnswersReader(index: Index, attachToAllItems: Boolean, document: Document): Read[DocumentDomain] =
    (
      DocumentReferenceNumberPage(index).reader,
      AddLineItemNumberYesNoPage(index).filterOptionalDependent(identity)(LineItemNumberPage(index).reader),
      AddAdditionalInformationYesNoPage(index).filterOptionalDependent(identity)(AdditionalInformationPage(index).reader)
    ).map(SupportDocumentDomain.apply(attachToAllItems, document, _, _, _)(index))
}

case class TransportDocumentDomain(
  attachToAllItems: Boolean,
  document: Document,
  referenceNumber: String
)(override val index: Index)
    extends DocumentDomain

object TransportDocumentDomain {

  implicit def userAnswersReader(index: Index, attachToAllItems: Boolean, document: Document): Read[DocumentDomain] =
    DocumentReferenceNumberPage(index).reader.map(TransportDocumentDomain.apply(attachToAllItems, document, _)(index))
}

sealed trait PreviousDocumentDomain extends DocumentDomain

object PreviousDocumentDomain {

  implicit def userAnswersReader(index: Index, attachToAllItems: Boolean, document: Document): Read[DocumentDomain] =
    if (attachToAllItems) {
      PreviousDocumentConsignmentLevelDomain.userAnswersReader(index, document)
    } else {
      PreviousDocumentItemLevelDomain.userAnswersReader(index, document)
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

  implicit def userAnswersReader(index: Index, document: Document): Read[DocumentDomain] =
    (
      DocumentReferenceNumberPage(index).reader,
      AddTypeOfPackageYesNoPage(index).filterOptionalDependent(identity)(PackageDomain.userAnswersReader(index)),
      DeclareQuantityOfGoodsYesNoPage(index).filterOptionalDependent(identity)(QuantityDomain.userAnswersReader(index)),
      AddAdditionalInformationYesNoPage(index).filterOptionalDependent(identity)(AdditionalInformationPage(index).reader)
    ).map(PreviousDocumentItemLevelDomain.apply(document, _, _, _, _)(index))
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

  implicit def userAnswersReader(index: Index, document: Document): Read[DocumentDomain] =
    (
      DocumentReferenceNumberPage(index).reader,
      AddAdditionalInformationYesNoPage(index).filterOptionalDependent(identity)(AdditionalInformationPage(index).reader)
    ).map(PreviousDocumentConsignmentLevelDomain.apply(document, _, _)(index))
}
