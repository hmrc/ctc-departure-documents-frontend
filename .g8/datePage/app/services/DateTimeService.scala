package services

import java.time.{Clock, LocalDate, LocalDateTime}
import javax.inject.Inject

class DateTimeService @Inject() (clock: Clock) {

  def today: LocalDate = LocalDate.now(clock)

  def now: LocalDateTime = LocalDateTime.now(clock)

  def plusMinusDays(n: Int): LocalDate = today.plusDays(n)
}
