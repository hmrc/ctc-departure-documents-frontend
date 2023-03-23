package pages

import pages.behaviours.PageBehaviours

class AddAnotherDocumentPageSpec extends PageBehaviours {

  "AddAnotherDocumentPage" - {

    beRetrievable[Boolean](AddAnotherDocumentPage)

    beSettable[Boolean](AddAnotherDocumentPage)

    beRemovable[Boolean](AddAnotherDocumentPage)
  }
}
