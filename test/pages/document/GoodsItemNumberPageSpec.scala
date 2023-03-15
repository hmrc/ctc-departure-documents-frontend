package pages.document

import pages.behaviours.PageBehaviours

class GoodsItemNumberPageSpec extends PageBehaviours {

  "GoodsItemNumberPage" - {

    beRetrievable[String](GoodsItemNumberPage)

    beSettable[String](GoodsItemNumberPage)

    beRemovable[String](GoodsItemNumberPage)
  }
}
