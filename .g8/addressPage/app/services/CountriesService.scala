package services

import connectors.ReferenceDataConnector
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference.Country
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountriesService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def doesCountryRequireZip(country: Country)(implicit hc: HeaderCarrier): Future[Boolean] =
    referenceDataConnector
      .getCountriesWithoutZipCountry(country.code.code)
      .map {
        _ => true
      }
      .recover {
        case _: NoReferenceDataFoundException => false
      }

}
