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
echo "$package$.$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.heading = $title$" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.$option1key;format="decap"$ = $option1msg$" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.$option2key;format="decap"$ = $option2msg$" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.checkYourAnswersLabel = $title$" >> ../conf/messages.en
echo "$package$.$className;format="decap"$.error.required = Select  $title$" >> ../conf/messages.en

echo "Adding to ModelGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$: Arbitrary[models.$package$.$className$] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(models.$package$.$className$.values)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

if grep -q "override val getValue" ../test/views/behaviours/YesNoViewBehaviours.scala; then
  echo "override val 'getValue' already exists in YesNoViewBehaviours. No changes made."
else
  awk '/trait YesNoViewBehaviours extends RadioViewBehaviours\[Boolean\] \{/{
      print;
      print "";
      print "  override val getValue: Boolean => String = _.toString";
      next;
  }
  { print }' ../test/views/behaviours/YesNoViewBehaviours.scala > tmp && mv tmp ../test/views/behaviours/YesNoViewBehaviours.scala
  echo "override val 'getValue' has been added to YesNoViewBehaviours."
fi

if grep -q "val getValue" ../test/views/behaviours/RadioViewBehaviours.scala; then
  echo "val 'getValue' already exists in RadioViewBehaviours. No changes made."
else
  awk '/trait RadioViewBehaviours\[T\] extends QuestionViewBehaviours\[T\] \{/{
      print;
      print "";
      print "  val getValue: T => String";
      next;
  }
  { print }' ../test/views/behaviours/RadioViewBehaviours.scala > tmp && mv tmp ../test/views/behaviours/RadioViewBehaviours.scala
  echo "val 'getValue' has been added to RadioViewBehaviours."
fi

echo "Migration $className;format="snake"$ completed"
