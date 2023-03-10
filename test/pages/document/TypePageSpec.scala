package pages.document

import models.reference.DocumentType
import pages.behaviours.PageBehaviours

class TypePageSpec extends PageBehaviours {

  "TypePage" - {

    beRetrievable[DocumentType](TypePage)

    beSettable[DocumentType](TypePage)

    beRemovable[DocumentType](TypePage)
  }
}
