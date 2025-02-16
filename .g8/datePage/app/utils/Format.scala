package utils

import java.time.format.DateTimeFormatter
import java.time.LocalDate

object Format {

  val formatterNoMillis: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

  implicit class RichLocalDate(localDate: LocalDate) {
    def formatAsString: String = localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
  }
}
