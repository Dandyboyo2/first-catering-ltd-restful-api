package models

import java.time.LocalDateTime
import mongoDateTimeFormat.MongoDateTimeFormat
import play.api.libs.json.{Json, OFormat}

class EmployeeSession(cardID: String, lastUpdated: LocalDateTime)

object EmployeeSession extends MongoDateTimeFormat {
  implicit val format: OFormat[EmployeeSession] = Json.format
}
