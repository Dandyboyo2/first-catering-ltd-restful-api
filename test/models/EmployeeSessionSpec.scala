package models

import mongoDateTimeFormat.MongoDateTimeFormat
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json.Json

import java.time.LocalDateTime

class EmployeeSessionSpec extends FreeSpec with MustMatchers with MongoDateTimeFormat {

  "EmployeeSession model" - {
    val employeeSessionID = "QWerty1234567890"
    val employeeSessionTime = LocalDateTime.now

    "must serialise into JSON" in {
      val employeeSession = EmployeeSession(
        cardID = employeeSessionID,
        lastUpdated = employeeSessionTime
      )

      val expectedJson = Json.obj(
        "cardID" -> employeeSessionID,
        "lastUpdated" -> employeeSessionTime
      )
      Json.toJson(employeeSession) mustEqual expectedJson
    }

    "must deserialise from JSON" in {
      val json = Json.obj(
        "cardID" -> employeeSessionID,
        "lastUpdated" -> employeeSessionTime
      )

      val expectedEmployee = EmployeeSession(
        cardID = employeeSessionID,
        lastUpdated = employeeSessionTime.minusHours(1)
      )
      json.as[EmployeeSession] mustEqual expectedEmployee
    }
  }
}
