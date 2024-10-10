package models.reference

import cats.Order
import models.Selectable
import play.api.libs.json.{Json, OFormat}

case class Country(code: CountryCode, description: String) extends Selectable {
  override def toString: String = s"\$description - \${code.code}"

  override val value: String = code.code
}

object Country {
  implicit val format: OFormat[Country] = Json.format[Country]

  implicit val order: Order[Country] = (x: Country, y: Country) => (x, y).compareBy(_.description, _.code.code)
}
