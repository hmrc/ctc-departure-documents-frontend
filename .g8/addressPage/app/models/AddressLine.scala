package models

import models.domain.StringFieldRegex._
import play.api.i18n.Messages

import scala.util.matching.Regex

sealed trait AddressLine {
  val field: String
  def arg(implicit messages: Messages): String = messages(s"address.\$field")
}

object AddressLine {

  case object Country extends AddressLine {
    override val field: String = "country"
  }

  sealed trait AddressLineWithValidation extends AddressLine {
    val length: Int
    val regex: Regex
  }

  case object NumberAndStreet extends AddressLineWithValidation {
    override val field: String = "numberAndStreet"
    override val length: Int   = 35
    override val regex: Regex  = stringFieldRegex
  }

  case object City extends AddressLineWithValidation {
    override val field: String = "city"
    override val length: Int   = 35
    override val regex: Regex  = stringFieldRegex
  }

  case object PostalCode extends AddressLineWithValidation {
    override val field: String = "postalCode"
    override val length: Int   = 9
    override val regex: Regex  = alphaNumericWithSpacesRegex
  }
}
