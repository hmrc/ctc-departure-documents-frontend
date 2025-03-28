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

package pages.external

import models.Index
import pages.ReadOnlyPage
import pages.sections.external.DocumentsSection
import play.api.libs.json.JsPath

import java.util.UUID

case class DocumentPage(itemIndex: Index, documentIndex: Index) extends ReadOnlyPage[UUID] {

  override def path: JsPath = DocumentsSection(itemIndex).path \ documentIndex.position \ toString

  override def toString: String = "document"
}
