package pages.document

import pages.behaviours.PageBehaviours

class AddSupportingItemNumberYesNoPageSpec extends PageBehaviours {

  "AddSupportingItemNumberYesNoPage" - {

    beRetrievable[Boolean](AddSupportingItemNumberYesNoPage)

    beSettable[Boolean](AddSupportingItemNumberYesNoPage)

    beRemovable[Boolean](AddSupportingItemNumberYesNoPage)
  }
}
