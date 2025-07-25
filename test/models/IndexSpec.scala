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

package models

import base.SpecBase

class IndexSpec extends SpecBase {

  "Index display must return correct Int" in {
    Index(0).display mustEqual 1
  }

  "indexPathBindable" - {
    val binder = Index.indexPathBindable
    val key    = "index"

    "bind a valid index" in {
      binder.bind(key, "1").value mustEqual Index(0)
    }

    "fail to bind an index with negative value" in {
      binder.bind(key, "-1").left.value mustEqual "Index binding failed"
    }

    "unbind an index" in {
      binder.unbind(key, Index(0)) mustEqual "1"
    }
  }
}
