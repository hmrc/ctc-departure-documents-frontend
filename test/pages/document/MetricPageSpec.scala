package pages.document

import models.reference.Metric
import pages.behaviours.PageBehaviours

class MetricPageSpec extends PageBehaviours {

  "MetricPage" - {

    beRetrievable[Metric](MetricPage)

    beSettable[Metric](MetricPage)

    beRemovable[Metric](MetricPage)
  }
}
