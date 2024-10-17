package views.behaviours

import models.Radioable

trait EnumerableViewBehaviours[T <: Radioable[T]] extends RadioViewBehaviours[T] {
  override val getValue: T => String = _.code
}
