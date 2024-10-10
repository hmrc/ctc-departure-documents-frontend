#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.$package$.routes"

if [ ! -f ../conf/app.$package$.routes ]; then
  echo "Write into app.routes file"
  awk '
  /# microservice specific routes/ {
    print;
    print "";
    next;
  }
  /^\$/ {
    if (!printed) {
      printed = 1;
      print "->         /                                            app.$package$.Routes";
      next;
    }
    print;
    next;
  }
  {
    if (!printed) {
      printed = 1;
      print "->         /                                            app.$package$.Routes";
    }
    print
  }' ../conf/app.routes > tmp && mv tmp ../conf/app.routes
fi

echo "" >> ../conf/app.$package$.routes
echo "GET        /$package;format="packaged"$/$title;format="normalize"$/:lrn                        controllers.$package$.$className$Controller.onPageLoad(lrn: LocalReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.$package$.routes
echo "POST       /$package;format="packaged"$/$title;format="normalize"$/:lrn                        controllers.$package$.$className$Controller.onSubmit(lrn: LocalReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.$package$.routes

echo "GET        /$package;format="packaged"$/change-$title;format="normalize"$/:lrn                 controllers.$package$.$className$Controller.onPageLoad(lrn: LocalReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.$package$.routes
echo "POST       /$package;format="packaged"$/change-$title;format="normalize"$/:lrn                 controllers.$package$.$className$Controller.onSubmit(lrn: LocalReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.$package$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "address.postalCode = postal code" >> ../conf/messages.en
echo "address.country = country" >> ../conf/messages.en
echo "address.numberAndStreet = number and street name" >> ../conf/messages.en
echo "address.city = town or city" >> ../conf/messages.en
echo "address.streetNumber = street number" >> ../conf/messages.en

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.heading = $title$" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.numberAndStreet = Number and street name" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.city = Town or city" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.postalCode = Postal code" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.postalCode.optional = Postal code (optional)" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.checkYourAnswersLabel = $title$" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.error.postalCode.invalid = The postal code must only include letters a to z, numbers 0 to 9 and spaces" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.error.invalid = The {0} must only include letters a to z without accents, numbers 0 to 9, ampersands (&), apostrophes, at signs (@), forward slashes, full stops, hyphens, question marks and spaces" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.error.required = Enter the {0} for {1}’s address" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.error.length = The {0} must be {2} characters or less" >> ../conf/messages.en

if grep -q "def getCountriesWithoutZipCountry" ../app/connectors/ReferenceDataConnector.scala; then
  echo "Function 'getCountriesWithoutZipCountry' already exists in ReferenceDataConnector. No changes made."
else
  awk '/class ReferenceDataConnector @Inject\(\) \(config: FrontendAppConfig, http: HttpClientV2\) extends Logging \{/{
      print;
      print "";
      print "  def getCountriesWithoutZipCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[CountryCode] = {";
      print "    val url = url\"\${config.referenceDataUrl}/lists/CountryWithoutZip\"";
      print "    http";
      print "      .get(url)";
      print "      .transform(_.withQueryStringParameters(\"data.code\" -> code))";
      print "      .setHeader(version2Header*)";
      print "      .execute[NonEmptySet[CountryCode]]";
      print "      .map(_.head)";
      print "  }";
      next;
  }
  { print }' ../app/connectors/ReferenceDataConnector.scala > tmp && mv tmp ../app/connectors/ReferenceDataConnector.scala
  echo "Function 'getCountriesWithoutZipCountry' has been added to ReferenceDataConnector."
fi

if grep -q "implicit lazy val arbitraryCountry" ../test/generators/ModelGenerators.scala; then
  echo "implicit lazy val 'arbitraryCountry' already exists in ModelGenerators. No changes made."
else
  awk '/self: Generators =>/ {\
      print;\
      print "";\
      print "  implicit lazy val arbitraryCountry: Arbitrary[Country] =";\
      print "    Arbitrary {";\
      print "      for {";\
      print "        code <- arbitrary[CountryCode]";\
      print "        name <- nonEmptyString";\
      print "      } yield Country(code, name)";\
      print "    }";\
      next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala
      echo "implicit lazy val 'arbitraryCountry' has been added to ModelGenerators."
fi

if grep -q "implicit lazy val arbitraryCountryCode" ../test/generators/ModelGenerators.scala; then
  echo "implicit lazy val 'arbitraryCountryCode' already exists in ModelGenerators. No changes made."
else
  awk '/self: Generators =>/ {\
      print;\
      print "";\
      print "  implicit lazy val arbitraryCountryCode: Arbitrary[CountryCode] =";\
      print "    Arbitrary {";\
      print "      Gen";\
      print "        .pick(2, '\''A'\'' to '\''Z'\'')";\
      print "        .map(";\
      print "          code => CountryCode(code.mkString)";\
      print "        )";\
      print "    }";\
      next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala
      echo "implicit lazy val 'arbitraryCountryCode' has been added to ModelGenerators."
fi

if grep -q "\^implicit lazy val arbitraryDynamicAddress\$" ../test/generators/ModelGenerators.scala; then
  echo "implicit lazy val 'arbitraryDynamicAddress' already exists in ModelGenerators. No changes made."
else
  awk '/self: Generators =>/ {\
      print;\
      print "";\
      print "  implicit lazy val arbitraryDynamicAddress: Arbitrary[DynamicAddress] = {";\
      print "    import models.AddressLine._";\
      print "";\
      print "    Arbitrary {";\
      print "      for {";\
      print "        numberAndStreet <- stringsWithMaxLength(NumberAndStreet.length, Gen.alphaNumChar)";\
      print "        city            <- stringsWithMaxLength(City.length, Gen.alphaNumChar)";\
      print "        postalCode      <- stringsWithMaxLength(PostalCode.length, Gen.alphaNumChar)";\
      print "      } yield DynamicAddress(numberAndStreet, city, Some(postalCode))";\
      print "    }";\
      print "  }";\
      next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala
      echo "implicit lazy val 'arbitraryDynamicAddress' has been added to ModelGenerators."
fi

if grep -q "\^lazy val arbitraryDynamicAddressWithRequiredPostalCode\$" ../test/generators/ModelGenerators.scala; then
  echo "lazy val 'arbitraryDynamicAddressWithRequiredPostalCode' already exists in ModelGenerators. No changes made."
else
  awk '/self: Generators =>/ {\
      print;\
      print "";\
      print "  lazy val arbitraryDynamicAddressWithRequiredPostalCode: Arbitrary[DynamicAddress] = {";\
      print "    import models.AddressLine._";\
      print "";\
      print "    Arbitrary {";\
      print "      for {";\
      print "        numberAndStreet <- stringsWithMaxLength(NumberAndStreet.length, Gen.alphaNumChar)";\
      print "        city            <- stringsWithMaxLength(City.length, Gen.alphaNumChar)";\
      print "        postalCode      <- stringsWithMaxLength(PostalCode.length, Gen.alphaNumChar)";\
      print "      } yield DynamicAddress(numberAndStreet, city, Some(postalCode))";\
      print "    }";\
      print "  }";\
      next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala
      echo "lazy val 'arbitraryDynamicAddressWithRequiredPostalCode' has been added to ModelGenerators."
fi

echo "Migration $className;format="snake"$ completed"
