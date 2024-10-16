package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{Clock, Duration, ZoneId}

class DateTimeServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks {

  "currentDateTime" - {

    "must return different times for different system clocks" in {

      val hourGen = Gen.choose(-12, 12)
      forAll(hourGen) {
        hour =>
          val firstZoneId = "UTC"
          val secondZoneId = firstZoneId + (hour match {
            case _ if hour < 0 => s"\$hour"
            case _             => s"+\$hour"
          })

          val clock1: Clock = Clock.system(ZoneId.of(firstZoneId))
          val clock2: Clock = Clock.system(ZoneId.of(secondZoneId))

          val dataTimeService1 = new DateTimeService(clock1)
          val dataTimeService2 = new DateTimeService(clock2)

          val duration = Duration.between(dataTimeService1.now, dataTimeService2.now)

          duration.toHours mustEqual hour
      }
    }
  }
}
