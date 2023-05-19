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

package forms

import config.FrontendAppConfig
import forms.mappings.Mappings
import models.reference.Document
import models.{ConsignmentLevelDocuments, Selectable, SelectableList}
import play.api.data.Form

import javax.inject.Inject

class SelectableFormProvider @Inject() extends Mappings {

  def apply[T <: Selectable](prefix: String, selectableList: SelectableList[T], args: Any*): Form[T] =
    Form(
      "value" -> selectable[T](selectableList, s"$prefix.error.required", args)
    )
}

class DocumentFormProvider @Inject() extends Mappings {

  def apply(prefix: String, selectableList: SelectableList[Document], consignmentLevelDocuments: ConsignmentLevelDocuments, args: Any*)(implicit
    config: FrontendAppConfig
  ): Form[Document] =
    Form(
      "value" -> selectable[Document](selectableList, s"$prefix.error.required", args)
        .verifying(
          maxLimit(consignmentLevelDocuments, s"$prefix.error.maxLimitReached")
        )
    )
}
